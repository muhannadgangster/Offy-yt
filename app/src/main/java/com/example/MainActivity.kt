package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.add.AddMediaDialog
import com.example.ui.components.CustomTitleDialog
import com.example.ui.components.YouTubeBottomBar
import com.example.ui.components.YouTubeTopBar
import com.example.ui.home.HomeScreen
import com.example.ui.library.BackupSyncDialog
import com.example.ui.library.LibraryScreen
import com.example.ui.library.ProfileDialog
import com.example.ui.player.PlayerScreen
import com.example.ui.shorts.ShortsScreen
import com.example.ui.subscriptions.SubscriptionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.YouTubeBlack
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MyTubeApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MyTubeApp(viewModel: MainViewModel) {
    // Request storage / media permissions on app launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // When granted, rescan device media
        if (permissions.values.any { it }) {
            viewModel.refreshVideos()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissionsToRequest)
    }

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val currentPlayingVideo by viewModel.currentPlayingVideo.collectAsStateWithLifecycle()
    val videoToRename by viewModel.videoToRename.collectAsStateWithLifecycle()
    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsStateWithLifecycle()
    val isBackupDialogOpen by viewModel.isBackupDialogOpen.collectAsStateWithLifecycle()
    val isProfileDialogOpen by viewModel.isProfileDialogOpen.collectAsStateWithLifecycle()

    val rawVideos by viewModel.rawVideos.collectAsStateWithLifecycle()
    val homeFeedVideos by viewModel.homeFeedVideos.collectAsStateWithLifecycle()
    val shortsVideos by viewModel.shortsVideos.collectAsStateWithLifecycle()
    val subscribedVideos by viewModel.subscribedVideos.collectAsStateWithLifecycle()
    val historyVideos by viewModel.historyVideos.collectAsStateWithLifecycle()
    val savedVideos by viewModel.savedVideos.collectAsStateWithLifecycle()
    val likedVideos by viewModel.likedVideos.collectAsStateWithLifecycle()
    val thumbnailBitmaps by viewModel.thumbnailBitmaps.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    // Navigation Back Handling
    BackHandler(enabled = currentPlayingVideo != null || isSearchActive || selectedTab != 0) {
        when {
            currentPlayingVideo != null -> viewModel.closePlayer()
            isSearchActive -> viewModel.setSearchActive(false)
            selectedTab != 0 -> viewModel.selectTab(0)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(YouTubeBlack)
    ) {
        Scaffold(
            topBar = {
                // TopBar is visible when no video is currently occupying the full player screen
                if (currentPlayingVideo == null && selectedTab != 1) {
                    YouTubeTopBar(
                        isSearchActive = isSearchActive,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSearchActiveToggle = { viewModel.setSearchActive(it) },
                        userProfile = userProfile,
                        onProfileClick = { viewModel.openProfileDialog() }
                    )
                }
            },
            bottomBar = {
                // BottomBar is visible when full player is not opened
                if (currentPlayingVideo == null) {
                    YouTubeBottomBar(
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            },
            containerColor = YouTubeBlack
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        videos = homeFeedVideos,
                        shortsVideos = shortsVideos,
                        thumbnailBitmaps = thumbnailBitmaps,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.selectCategory(it) },
                        isLoading = isLoading,
                        onRefresh = { viewModel.refreshVideos() },
                        onVideoClick = { viewModel.playVideo(it) },
                        onShortClick = { viewModel.playVideo(it) },
                        onEditTitle = { viewModel.openRenameDialog(it) },
                        onToggleSubscribe = { viewModel.toggleSubscribe(it) },
                        onToggleSave = { viewModel.toggleSave(it) },
                        onOpenAddDialog = { viewModel.openAddDialog() }
                    )

                    1 -> ShortsScreen(
                        shortsList = shortsVideos,
                        onToggleLike = { viewModel.toggleLike(it) },
                        onToggleDislike = { viewModel.toggleDislike(it) },
                        onToggleSubscribe = { viewModel.toggleSubscribe(it) },
                        onEditTitle = { viewModel.openRenameDialog(it) },
                        onSaveNotes = { video, notes -> viewModel.saveNotes(video, notes) }
                    )

                    3 -> SubscriptionsScreen(
                        subscribedVideos = subscribedVideos,
                        thumbnailBitmaps = thumbnailBitmaps,
                        onVideoClick = { viewModel.playVideo(it) },
                        onEditTitle = { viewModel.openRenameDialog(it) },
                        onToggleSubscribe = { viewModel.toggleSubscribe(it) },
                        onToggleSave = { viewModel.toggleSave(it) },
                        onExploreHome = { viewModel.selectTab(0) }
                    )

                    4 -> LibraryScreen(
                        userProfile = userProfile,
                        historyVideos = historyVideos,
                        savedVideos = savedVideos,
                        likedVideos = likedVideos,
                        thumbnailBitmaps = thumbnailBitmaps,
                        onVideoClick = { viewModel.playVideo(it) },
                        onOpenBackupDialog = { viewModel.openBackupDialog() },
                        onOpenProfileDialog = { viewModel.openProfileDialog() }
                    )
                }
            }
        }

        // Fullscreen/Floating Player Overlay
        AnimatedVisibility(
            visible = currentPlayingVideo != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            currentPlayingVideo?.let { playingVideo ->
                PlayerScreen(
                    video = playingVideo,
                    upNextVideos = rawVideos,
                    thumbnailBitmaps = thumbnailBitmaps,
                    onClose = { viewModel.closePlayer() },
                    onEditTitle = { viewModel.openRenameDialog(playingVideo) },
                    onToggleSubscribe = { viewModel.toggleSubscribe(playingVideo) },
                    onToggleLike = { viewModel.toggleLike(playingVideo) },
                    onToggleDislike = { viewModel.toggleDislike(playingVideo) },
                    onToggleSave = { viewModel.toggleSave(playingVideo) },
                    onSelectUpNext = { viewModel.playVideo(it) },
                    onEditUpNextTitle = { viewModel.openRenameDialog(it) }
                )
            }
        }

        // Custom Title Rename Dialog (triggered by small '+' icon)
        if (videoToRename != null) {
            CustomTitleDialog(
                video = videoToRename!!,
                onDismiss = { viewModel.closeRenameDialog() },
                onSave = { newTitle, category ->
                    viewModel.saveCustomTitle(newTitle, category)
                }
            )
        }

        // Add / Import Video Dialog
        if (isAddDialogOpen) {
            AddMediaDialog(
                onDismiss = { viewModel.closeAddDialog() },
                onImportVideo = { uri, title, cat ->
                    viewModel.importVideo(uri, title, cat)
                }
            )
        }

        // Backup & Cloud Sync Dialog
        if (isBackupDialogOpen) {
            BackupSyncDialog(
                onDismiss = { viewModel.closeBackupDialog() },
                onExportBackup = { viewModel.exportBackupJson() },
                onImportBackup = { json, onComplete ->
                    viewModel.importBackupJson(json, onComplete)
                }
            )
        }

        // User Profile & Account Dialog
        if (isProfileDialogOpen) {
            ProfileDialog(
                userProfile = userProfile,
                onDismiss = { viewModel.closeProfileDialog() },
                onSaveProfile = { name, handle ->
                    viewModel.updateProfile(name, handle)
                }
            )
        }
    }
}
