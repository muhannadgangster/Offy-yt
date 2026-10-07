package com.example.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import com.example.data.local.AppDatabase
import com.example.data.local.CustomVideoMetadata
import com.example.data.model.UserProfile
import com.example.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class MediaRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.videoMetadataDao()

    // In-memory cache for generated video thumbnails
    private val memoryCache: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(50) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    private val sampleCategories = listOf("All", "Music", "Gaming", "Shorts", "Tech", "Cinematic", "Podcasts", "Saved")

    val allMetadata: Flow<List<CustomVideoMetadata>> = dao.getAllMetadata()

    /**
     * Scan device MediaStore for local video files.
     * If no videos are present on the device, seeds demo local videos so the user can immediately experience the app.
     */
    suspend fun loadAllVideos(): List<VideoItem> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<VideoItem>()
        val metadataList = dao.getAllMetadata().first()
        val metadataMap = metadataList.associateBy { it.videoUri }

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATA
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleCol = c.getColumnIndex(MediaStore.Video.Media.TITLE)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val widthCol = c.getColumnIndex(MediaStore.Video.Media.WIDTH)
                val heightCol = c.getColumnIndex(MediaStore.Video.Media.HEIGHT)
                val dataCol = c.getColumnIndex(MediaStore.Video.Media.DATA)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    val displayName = c.getString(nameCol) ?: "Video_$id"
                    val title = if (titleCol != -1) c.getString(titleCol) ?: displayName else displayName
                    val duration = c.getLong(durCol)
                    val size = c.getLong(sizeCol)
                    val width = if (widthCol != -1) c.getInt(widthCol) else 1920
                    val height = if (heightCol != -1) c.getInt(heightCol) else 1080
                    val path = if (dataCol != -1) c.getString(dataCol) ?: "" else ""

                    val isVertical = (height > width && width > 0) || (duration in 1..65_000 && displayName.contains("short", ignoreCase = true))

                    val uriStr = contentUri.toString()
                    val meta = metadataMap[uriStr]

                    val cleanTitle = title.substringBeforeLast(".")
                    val channelInfo = deriveChannelInfo(path, displayName)

                    videoList.add(
                        VideoItem(
                            id = id,
                            uri = contentUri,
                            path = path,
                            originalTitle = cleanTitle,
                            customTitle = meta?.customTitle,
                            durationMs = duration,
                            sizeBytes = size,
                            width = if (width > 0) width else 1920,
                            height = if (height > 0) height else 1080,
                            isVertical = isVertical,
                            channelName = channelInfo.first,
                            channelHandle = channelInfo.second,
                            channelAvatarColor = channelInfo.third,
                            category = meta?.category ?: if (isVertical) "Shorts" else "All",
                            isSubscribed = meta?.isSubscribed ?: false,
                            isLiked = meta?.isLiked ?: false,
                            isDisliked = meta?.isDisliked ?: false,
                            isSaved = meta?.isSavedToLibrary ?: false,
                            likeCount = 120 + abs((id % 880).toInt()) + (meta?.likeCountOffset ?: 0),
                            notes = meta?.notes ?: "",
                            lastPositionMs = meta?.lastPositionMs ?: 0L,
                            lastWatchedTimestamp = meta?.lastWatchedTimestamp ?: 0L
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Also check any imported videos in app private storage
        val privateVideos = loadPrivateStorageVideos(metadataMap)
        videoList.addAll(privateVideos)

        // If still empty (e.g., fresh emulator), seed demo videos
        if (videoList.isEmpty()) {
            val seeded = seedDemoVideos(metadataMap)
            videoList.addAll(seeded)
        }

        videoList
    }

    private fun loadPrivateStorageVideos(metadataMap: Map<String, CustomVideoMetadata>): List<VideoItem> {
        val list = mutableListOf<VideoItem>()
        val dir = File(context.filesDir, "imported_videos")
        if (dir.exists()) {
            val files = dir.listFiles { f -> f.extension in listOf("mp4", "mkv", "webm", "mov") } ?: emptyArray()
            files.forEachIndexed { index, file ->
                val uri = Uri.fromFile(file)
                val uriStr = uri.toString()
                val meta = metadataMap[uriStr]
                val id = 10000L + index
                val name = file.nameWithoutExtension
                val isVertical = name.contains("short", ignoreCase = true)
                val channel = deriveChannelInfo(file.absolutePath, file.name)
                list.add(
                    VideoItem(
                        id = id,
                        uri = uri,
                        path = file.absolutePath,
                        originalTitle = name,
                        customTitle = meta?.customTitle,
                        durationMs = 45000L,
                        sizeBytes = file.length(),
                        width = if (isVertical) 1080 else 1920,
                        height = if (isVertical) 1920 else 1080,
                        isVertical = isVertical,
                        channelName = channel.first,
                        channelHandle = channel.second,
                        channelAvatarColor = channel.third,
                        category = meta?.category ?: if (isVertical) "Shorts" else "All",
                        isSubscribed = meta?.isSubscribed ?: false,
                        isLiked = meta?.isLiked ?: false,
                        isDisliked = meta?.isDisliked ?: false,
                        isSaved = meta?.isSavedToLibrary ?: false,
                        likeCount = 250 + (index * 42) + (meta?.likeCountOffset ?: 0),
                        notes = meta?.notes ?: "",
                        lastPositionMs = meta?.lastPositionMs ?: 0L,
                        lastWatchedTimestamp = meta?.lastWatchedTimestamp ?: 0L
                    )
                )
            }
        }
        return list
    }

    /**
     * Seeds realistic sample videos for seamless testing when device gallery is empty.
     */
    private suspend fun seedDemoVideos(metadataMap: Map<String, CustomVideoMetadata>): List<VideoItem> {
        val samplesDir = File(context.filesDir, "demo_videos")
        if (!samplesDir.exists()) {
            samplesDir.mkdirs()
        }

        val demoSpecs = listOf(
            Triple("Cinematic Nature 4K - Wildlife Odyssey", false, "Nature Lab"),
            Triple("Top 10 Coding Hacks for Kotlin Compose in 2026", false, "Android Masters"),
            Triple("Midnight Lo-Fi Beats - Relaxing Studio Stream", false, "Chill Beats FM"),
            Triple("Viral Parkour Flip Across Skyscrapers #shorts", true, "Urban Ninja"),
            Triple("Quick 30s Recipe: Japanese Souffle Pancake #shorts", true, "Chef Tasty"),
            Triple("Mountain Biking Down Razor Ridge #shorts", true, "Apex Outdoors"),
            Triple("Cyberpunk Tokyo Drone Walkthrough 60FPS", false, "Future Visuals")
        )

        val seededList = mutableListOf<VideoItem>()

        demoSpecs.forEachIndexed { index, (title, isVertical, channelName) ->
            val safeName = title.replace("[^a-zA-Z0-9]".toRegex(), "_") + ".mp4"
            val file = File(samplesDir, safeName)
            val rawRes = if (isVertical) com.example.R.raw.demo_vertical else com.example.R.raw.demo_horizontal
            if (!file.exists() || file.length() < 50_000L) {
                try {
                    context.resources.openRawResource(rawRes).use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val uri = Uri.fromFile(file)
            val uriStr = uri.toString()
            val meta = metadataMap[uriStr]
            val duration = if (isVertical) (15_000L + index * 5_000L) else (180_000L + index * 75_000L)

            val color = when (index % 6) {
                0 -> 0xFFE53935
                1 -> 0xFF1E88E5
                2 -> 0xFF43A047
                3 -> 0xFFFB8C00
                4 -> 0xFF8E24AA
                else -> 0xFF00ACC1
            }

            seededList.add(
                VideoItem(
                    id = 5000L + index,
                    uri = uri,
                    path = file.absolutePath,
                    originalTitle = title,
                    customTitle = meta?.customTitle,
                    durationMs = duration,
                    sizeBytes = 15_400_000L + index * 4_200_000L,
                    width = if (isVertical) 1080 else 1920,
                    height = if (isVertical) 1920 else 1080,
                    isVertical = isVertical,
                    channelName = channelName,
                    channelHandle = "@${channelName.lowercase().replace(" ", "")}",
                    channelAvatarColor = color,
                    category = if (isVertical) "Shorts" else sampleCategories[(index % 5) + 1],
                    isSubscribed = meta?.isSubscribed ?: (index == 1),
                    isLiked = meta?.isLiked ?: false,
                    isDisliked = meta?.isDisliked ?: false,
                    isSaved = meta?.isSavedToLibrary ?: (index == 0),
                    likeCount = 1420 + index * 312 + (meta?.likeCountOffset ?: 0),
                    notes = meta?.notes ?: if (index == 0) "Great reference video for color grading!" else "",
                    lastPositionMs = meta?.lastPositionMs ?: (if (index == 0) 45000L else 0L),
                    lastWatchedTimestamp = meta?.lastWatchedTimestamp ?: (if (index == 0) System.currentTimeMillis() - 3600000L else 0L)
                )
            )
        }

        return seededList
    }

    private fun deriveChannelInfo(path: String, filename: String): Triple<String, String, Long> {
        val parentFolder = if (path.isNotBlank()) File(path).parentFile?.name ?: "Gallery" else "Gallery"
        val cleanFolder = if (parentFolder.equals("0", ignoreCase = true) || parentFolder.equals("movies", ignoreCase = true)) {
            "MyTube Studio"
        } else {
            parentFolder.replace("_", " ").capitalizeWords()
        }

        val channelName = when {
            filename.contains("rec", ignoreCase = true) -> "Screen Capture HD"
            filename.contains("cam", ignoreCase = true) -> "Camera Vault"
            filename.contains("short", ignoreCase = true) -> "Shorts Official"
            filename.contains("music", ignoreCase = true) -> "Sound Wave Music"
            else -> cleanFolder
        }

        val handle = "@${channelName.lowercase().replace(" ", "")}"
        val colors = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFB8C00, 0xFF8E24AA, 0xFF00ACC1, 0xFFD81B60)
        val color = colors[abs(channelName.hashCode()) % colors.size]

        return Triple(channelName, handle, color)
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

    /**
     * Retrieves or generates a video thumbnail bitmap.
     */
    suspend fun getVideoThumbnail(video: VideoItem): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = video.uri.toString()
        memoryCache.get(cacheKey)?.let { return@withContext it }

        var bitmap: Bitmap? = null

        // 1. Try MediaStore Thumbnail (Android 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && video.uri.scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                bitmap = context.contentResolver.loadThumbnail(video.uri, Size(640, 360), null)
            } catch (_: Exception) { }
        }

        // 2. Try MediaMetadataRetriever from file / content
        if (bitmap == null) {
            val retriever = MediaMetadataRetriever()
            try {
                if (video.uri.scheme == ContentResolver.SCHEME_CONTENT) {
                    retriever.setDataSource(context, video.uri)
                } else if (video.path.isNotBlank() && File(video.path).exists()) {
                    retriever.setDataSource(video.path)
                }
                bitmap = retriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime
            } catch (_: Exception) {
            } finally {
                try { retriever.release() } catch (_: Exception) {}
            }
        }

        // 3. Fallback: generate a clean gradient placeholder bitmap with video title & duration
        if (bitmap == null) {
            bitmap = generateStyledThumbnail(video)
        }

        bitmap?.let { memoryCache.put(cacheKey, it) }
        bitmap
    }

    private fun generateStyledThumbnail(video: VideoItem): Bitmap {
        val width = if (video.isVertical) 360 else 640
        val height = if (video.isVertical) 640 else 360
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
        }

        // Background dark gradient
        val colors = intArrayOf(
            0xFF1E1E24.toInt(),
            (video.channelAvatarColor or 0xFF000000).toInt(),
            0xFF0F0F0F.toInt()
        )
        paint.shader = android.graphics.LinearGradient(
            0f, 0f, width.toFloat(), height.toFloat(),
            colors, null, android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Center play badge watermark
        paint.shader = null
        paint.color = 0x40FFFFFF
        canvas.drawCircle(width / 2f, height / 2f, 44f, paint)

        paint.color = 0x90FFFFFF.toInt()
        val path = android.graphics.Path().apply {
            moveTo(width / 2f - 14f, height / 2f - 22f)
            lineTo(width / 2f + 24f, height / 2f)
            lineTo(width / 2f - 14f, height / 2f + 22f)
            close()
        }
        canvas.drawPath(path, paint)

        return bitmap
    }

    /**
     * Import a video file from external URI (e.g. from Photo/Video picker) into private storage.
     */
    suspend fun importVideo(uri: Uri, customTitle: String?, category: String = "All"): VideoItem = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "imported_videos")
        if (!dir.exists()) dir.mkdirs()

        val fileName = "mytube_video_${System.currentTimeMillis()}.mp4"
        val destFile = File(dir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        val destUri = Uri.fromFile(destFile)
        val newMeta = CustomVideoMetadata(
            videoUri = destUri.toString(),
            customTitle = customTitle ?: destFile.nameWithoutExtension,
            category = category,
            isSavedToLibrary = true
        )
        dao.insertOrUpdate(newMeta)

        val channel = deriveChannelInfo(destFile.absolutePath, destFile.name)
        VideoItem(
            id = System.currentTimeMillis(),
            uri = destUri,
            path = destFile.absolutePath,
            originalTitle = destFile.nameWithoutExtension,
            customTitle = newMeta.customTitle,
            durationMs = 60000L,
            sizeBytes = destFile.length(),
            width = 1920,
            height = 1080,
            isVertical = category.equals("Shorts", ignoreCase = true),
            channelName = channel.first,
            channelHandle = channel.second,
            channelAvatarColor = channel.third,
            category = category,
            isSaved = true
        )
    }

    // --- Metadata CRUD Operations ---

    suspend fun setCustomTitle(videoUri: String, newTitle: String, category: String = "All") = withContext(Dispatchers.IO) {
        val existing = dao.getMetadataByUri(videoUri)
        val updated = (existing ?: CustomVideoMetadata(videoUri = videoUri)).copy(
            customTitle = newTitle,
            category = category
        )
        dao.insertOrUpdate(updated)
    }

    suspend fun toggleSubscribe(videoUri: String): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getMetadataByUri(videoUri)
        val newStatus = !(existing?.isSubscribed ?: false)
        val updated = (existing ?: CustomVideoMetadata(videoUri = videoUri)).copy(
            isSubscribed = newStatus
        )
        dao.insertOrUpdate(updated)
        newStatus
    }

    suspend fun toggleLike(videoUri: String): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
        val existing = dao.getMetadataByUri(videoUri)
        val isLiked = !(existing?.isLiked ?: false)
        val isDisliked = false
        val offset = if (isLiked) 1 else 0
        val updated = (existing ?: CustomVideoMetadata(videoUri = videoUri)).copy(
            isLiked = isLiked,
            isDisliked = isDisliked,
            likeCountOffset = offset
        )
        dao.insertOrUpdate(updated)
        Pair(isLiked, isDisliked)
    }

    suspend fun toggleDislike(videoUri: String): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
        val existing = dao.getMetadataByUri(videoUri)
        val isDisliked = !(existing?.isDisliked ?: false)
        val isLiked = false
        val updated = (existing ?: CustomVideoMetadata(videoUri = videoUri)).copy(
            isLiked = isLiked,
            isDisliked = isDisliked,
            likeCountOffset = if (isLiked) 1 else 0
        )
        dao.insertOrUpdate(updated)
        Pair(isLiked, isDisliked)
    }

    suspend fun toggleSaveToLibrary(videoUri: String): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getMetadataByUri(videoUri)
        val newSaved = !(existing?.isSavedToLibrary ?: false)
        val updated = (existing ?: CustomVideoMetadata(videoUri = videoUri)).copy(
            isSavedToLibrary = newSaved
        )
        dao.insertOrUpdate(updated)
        newSaved
    }

    suspend fun saveVideoNotes(videoUri: String, notes: String) = withContext(Dispatchers.IO) {
        val existing = dao.getMetadataByUri(videoUri)
        val updated = (existing ?: CustomVideoMetadata(videoUri = videoUri)).copy(
            notes = notes
        )
        dao.insertOrUpdate(updated)
    }

    suspend fun updateWatchProgress(videoUri: String, positionMs: Long) = withContext(Dispatchers.IO) {
        val existing = dao.getMetadataByUri(videoUri)
        val updated = (existing ?: CustomVideoMetadata(videoUri = videoUri)).copy(
            lastPositionMs = positionMs,
            watchCount = (existing?.watchCount ?: 0) + 1,
            lastWatchedTimestamp = System.currentTimeMillis()
        )
        dao.insertOrUpdate(updated)
    }

    // --- Cloud Sync & Backup / Restore Engine ---

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val metadataList = dao.getAllMetadata().first()
        val root = JSONObject()
        root.put("app", "MyTube")
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val array = JSONArray()
        metadataList.forEach { item ->
            val obj = JSONObject().apply {
                put("videoUri", item.videoUri)
                put("customTitle", item.customTitle ?: "")
                put("category", item.category)
                put("isSubscribed", item.isSubscribed)
                put("isLiked", item.isLiked)
                put("isSavedToLibrary", item.isSavedToLibrary)
                put("notes", item.notes)
                put("lastPositionMs", item.lastPositionMs)
                put("lastWatchedTimestamp", item.lastWatchedTimestamp)
            }
            array.put(obj)
        }
        root.put("items", array)
        root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Int = withContext(Dispatchers.IO) {
        var restoredCount = 0
        try {
            val root = JSONObject(jsonString)
            val array = root.getJSONArray("items")
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val meta = CustomVideoMetadata(
                    videoUri = obj.getString("videoUri"),
                    customTitle = obj.optString("customTitle").ifBlank { null },
                    category = obj.optString("category", "All"),
                    isSubscribed = obj.optBoolean("isSubscribed", false),
                    isLiked = obj.optBoolean("isLiked", false),
                    isSavedToLibrary = obj.optBoolean("isSavedToLibrary", false),
                    notes = obj.optString("notes", ""),
                    lastPositionMs = obj.optLong("lastPositionMs", 0L),
                    lastWatchedTimestamp = obj.optLong("lastWatchedTimestamp", 0L)
                )
                dao.insertOrUpdate(meta)
                restoredCount++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        restoredCount
    }
}
