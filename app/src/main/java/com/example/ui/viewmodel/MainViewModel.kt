package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserProfile
import com.example.data.model.VideoItem
import com.example.data.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)

    private val _rawVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val rawVideos: StateFlow<List<VideoItem>> = _rawVideos.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Shorts, 2: Add, 3: Subscriptions, 4: You
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentPlayingVideo = MutableStateFlow<VideoItem?>(null)
    val currentPlayingVideo: StateFlow<VideoItem?> = _currentPlayingVideo.asStateFlow()

    private val _isPlayerLocked = MutableStateFlow(false)
    val isPlayerLocked: StateFlow<Boolean> = _isPlayerLocked.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _videoToRename = MutableStateFlow<VideoItem?>(null)
    val videoToRename: StateFlow<VideoItem?> = _videoToRename.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _isBackupDialogOpen = MutableStateFlow(false)
    val isBackupDialogOpen: StateFlow<Boolean> = _isBackupDialogOpen.asStateFlow()

    private val _isProfileDialogOpen = MutableStateFlow(false)
    val isProfileDialogOpen: StateFlow<Boolean> = _isProfileDialogOpen.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _thumbnailBitmaps = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val thumbnailBitmaps: StateFlow<Map<String, Bitmap>> = _thumbnailBitmaps.asStateFlow()

    // Filtered feed for Home Screen based on Category and Search Query
    val homeFeedVideos: StateFlow<List<VideoItem>> = combine(
        _rawVideos,
        _searchQuery,
        _selectedCategory
    ) { list, query, category ->
        list.filter { video ->
            val matchesQuery = query.isBlank() ||
                video.displayTitle.contains(query, ignoreCase = true) ||
                video.channelName.contains(query, ignoreCase = true) ||
                video.category.contains(query, ignoreCase = true)

            val matchesCategory = when (category) {
                "All" -> true
                "Shorts" -> video.isVertical
                "Saved" -> video.isSaved
                "Music" -> video.category.equals("Music", ignoreCase = true)
                "Gaming" -> video.category.equals("Gaming", ignoreCase = true)
                "Tech" -> video.category.equals("Tech", ignoreCase = true)
                "Cinematic" -> video.category.equals("Cinematic", ignoreCase = true)
                else -> video.category.equals(category, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vertical videos or short clips for the Shorts tab
    val shortsVideos: StateFlow<List<VideoItem>> = _rawVideos.combine(_searchQuery) { list, query ->
        val filtered = list.filter { it.isVertical || it.durationMs in 1..65_000 }
        if (query.isBlank()) filtered else filtered.filter {
            it.displayTitle.contains(query, ignoreCase = true) || it.channelName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Subscriptions tab: videos from favorited/subscribed channels
    val subscribedVideos: StateFlow<List<VideoItem>> = _rawVideos.combine(_searchQuery) { list, query ->
        val subscribed = list.filter { it.isSubscribed }
        if (query.isBlank()) subscribed else subscribed.filter {
            it.displayTitle.contains(query, ignoreCase = true) || it.channelName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saved/Downloads for Library
    val savedVideos: StateFlow<List<VideoItem>> = _rawVideos.mapToState(viewModelScope) { list ->
        list.filter { it.isSaved }
    }

    // Watch history for Library
    val historyVideos: StateFlow<List<VideoItem>> = _rawVideos.mapToState(viewModelScope) { list ->
        list.filter { it.lastWatchedTimestamp > 0L }.sortedByDescending { it.lastWatchedTimestamp }
    }

    // Liked videos for Library
    val likedVideos: StateFlow<List<VideoItem>> = _rawVideos.mapToState(viewModelScope) { list ->
        list.filter { it.isLiked }
    }

    init {
        refreshVideos()
    }

    fun refreshVideos() {
        viewModelScope.launch {
            _isLoading.value = true
            val videos = repository.loadAllVideos()
            _rawVideos.value = videos
            _isLoading.value = false

            // Pre-fetch thumbnails in background
            videos.take(20).forEach { video ->
                fetchThumbnail(video)
            }
        }
    }

    fun fetchThumbnail(video: VideoItem) {
        val key = video.uri.toString()
        if (_thumbnailBitmaps.value.containsKey(key)) return

        viewModelScope.launch {
            val bitmap = repository.getVideoThumbnail(video)
            if (bitmap != null) {
                _thumbnailBitmaps.value = _thumbnailBitmaps.value + (key to bitmap)
            }
        }
    }

    fun selectTab(index: Int) {
        if (index == 2) {
            // Center Plus button opens add media dialog
            _isAddDialogOpen.value = true
        } else {
            _selectedTab.value = index
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun playVideo(video: VideoItem) {
        _currentPlayingVideo.value = video
        // Update watch history
        viewModelScope.launch {
            repository.updateWatchProgress(video.uri.toString(), 0L)
            updateVideoInList(video.copy(lastWatchedTimestamp = System.currentTimeMillis()))
        }
    }

    fun closePlayer() {
        _currentPlayingVideo.value = null
        _isPlayerLocked.value = false
    }

    fun togglePlayerLock() {
        _isPlayerLocked.value = !_isPlayerLocked.value
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun openRenameDialog(video: VideoItem) {
        _videoToRename.value = video
    }

    fun closeRenameDialog() {
        _videoToRename.value = null
    }

    fun saveCustomTitle(newTitle: String, category: String = "All") {
        val target = _videoToRename.value ?: return
        viewModelScope.launch {
            repository.setCustomTitle(target.uri.toString(), newTitle, category)
            val updated = target.copy(customTitle = newTitle, category = category)
            updateVideoInList(updated)
            if (_currentPlayingVideo.value?.uri == target.uri) {
                _currentPlayingVideo.value = updated
            }
            _videoToRename.value = null
        }
    }

    fun toggleSubscribe(video: VideoItem) {
        viewModelScope.launch {
            val newStatus = repository.toggleSubscribe(video.uri.toString())
            // Also update all videos sharing the same channel
            val updatedList = _rawVideos.value.map { item ->
                if (item.channelName == video.channelName || item.uri == video.uri) {
                    item.copy(isSubscribed = newStatus)
                } else item
            }
            _rawVideos.value = updatedList
            if (_currentPlayingVideo.value?.uri == video.uri) {
                _currentPlayingVideo.value = _currentPlayingVideo.value?.copy(isSubscribed = newStatus)
            }
        }
    }

    fun toggleLike(video: VideoItem) {
        viewModelScope.launch {
            val (isLiked, isDisliked) = repository.toggleLike(video.uri.toString())
            val updated = video.copy(
                isLiked = isLiked,
                isDisliked = isDisliked,
                likeCount = video.likeCount + (if (isLiked) 1 else -1)
            )
            updateVideoInList(updated)
            if (_currentPlayingVideo.value?.uri == video.uri) {
                _currentPlayingVideo.value = updated
            }
        }
    }

    fun toggleDislike(video: VideoItem) {
        viewModelScope.launch {
            val (isLiked, isDisliked) = repository.toggleDislike(video.uri.toString())
            val updated = video.copy(
                isLiked = isLiked,
                isDisliked = isDisliked,
                likeCount = if (video.isLiked) video.likeCount - 1 else video.likeCount
            )
            updateVideoInList(updated)
            if (_currentPlayingVideo.value?.uri == video.uri) {
                _currentPlayingVideo.value = updated
            }
        }
    }

    fun toggleSave(video: VideoItem) {
        viewModelScope.launch {
            val isSaved = repository.toggleSaveToLibrary(video.uri.toString())
            val updated = video.copy(isSaved = isSaved)
            updateVideoInList(updated)
            if (_currentPlayingVideo.value?.uri == video.uri) {
                _currentPlayingVideo.value = updated
            }
        }
    }

    fun saveNotes(video: VideoItem, notes: String) {
        viewModelScope.launch {
            repository.saveVideoNotes(video.uri.toString(), notes)
            val updated = video.copy(notes = notes)
            updateVideoInList(updated)
            if (_currentPlayingVideo.value?.uri == video.uri) {
                _currentPlayingVideo.value = updated
            }
        }
    }

    fun importVideo(uri: Uri, customTitle: String?, category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val newVideo = repository.importVideo(uri, customTitle, category)
            _rawVideos.value = listOf(newVideo) + _rawVideos.value
            fetchThumbnail(newVideo)
            _isLoading.value = false
            _isAddDialogOpen.value = false
        }
    }

    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    fun openBackupDialog() {
        _isBackupDialogOpen.value = true
    }

    fun closeBackupDialog() {
        _isBackupDialogOpen.value = false
    }

    fun openProfileDialog() {
        _isProfileDialogOpen.value = true
    }

    fun closeProfileDialog() {
        _isProfileDialogOpen.value = false
    }

    fun updateProfile(name: String, handle: String) {
        _userProfile.value = _userProfile.value.copy(
            name = name,
            handle = handle,
            avatarInitial = name.firstOrNull()?.uppercase() ?: "A"
        )
        _isProfileDialogOpen.value = false
    }

    suspend fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun importBackupJson(json: String, onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.importBackupJson(json)
            refreshVideos()
            onComplete(count)
        }
    }

    private fun updateVideoInList(updated: VideoItem) {
        _rawVideos.value = _rawVideos.value.map { if (it.uri == updated.uri) updated else it }
    }

    private fun <T, R> StateFlow<T>.mapToState(
        scope: kotlinx.coroutines.CoroutineScope,
        transform: (T) -> R
    ): StateFlow<R> {
        val initial = transform(this.value)
        val flow = MutableStateFlow(initial)
        scope.launch {
            this@mapToState.collect { value ->
                flow.value = transform(value)
            }
        }
        return flow.asStateFlow()
    }
}
