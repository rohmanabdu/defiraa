package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppTopBar
import com.example.ui.components.PlayerDialog
import com.example.ui.screens.ChannelsScreen
import com.example.ui.screens.ExtractScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RawM3uScreen
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceHighlight
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.M3uExtractorViewModel

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

class MainActivity : ComponentActivity() {

    private val viewModel: M3uExtractorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: M3uExtractorViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val savedPlaylists by viewModel.savedPlaylists.collectAsState()
    val isTestingStreams by viewModel.isTestingStreams.collectAsState()
    val currentlyPlayingChannel by viewModel.currentlyPlayingChannel.collectAsState()

    val navItems = listOf(
        NavTabItem("Ekstrak", Icons.Default.Tune, Icons.Outlined.Tune),
        NavTabItem("Channel", Icons.Default.LiveTv, Icons.Outlined.LiveTv, badgeCount = channels.size),
        NavTabItem("Editor .M3U", Icons.Default.Code, Icons.Outlined.Code),
        NavTabItem("Riwayat", Icons.Default.History, Icons.Outlined.History, badgeCount = savedPlaylists.size)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        if (isWideScreen) {
            // Tablet / Landscape NavigationRail Layout
            Row(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
                NavigationRail(
                    containerColor = DarkSurface,
                    contentColor = TextSecondary,
                    modifier = Modifier.testTag("app_navigation_rail")
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { viewModel.setSelectedTab(index) },
                            icon = {
                                NavIconWithBadge(item = item, isSelected = isSelected)
                            },
                            label = { Text(item.title, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = DarkSurface,
                                indicatorColor = CyanNeon,
                                unselectedIconColor = TextMuted,
                                selectedTextColor = CyanNeon,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_$index")
                        )
                    }
                }

                Scaffold(
                    topBar = {
                        AppTopBar(
                            title = "M3U Extractor",
                            subtitle = when (selectedTab) {
                                0 -> "Ekstrak Playlist dari URL APK"
                                1 -> "${channels.size} Channel Siaran Terdeteksi"
                                2 -> "Pratinjau & Ekspor File .M3U"
                                else -> "Riwayat Playlist Tersimpan"
                            },
                            onTestAllClick = if (selectedTab == 1 && channels.isNotEmpty()) {
                                { viewModel.testAllChannels() }
                            } else null,
                            isTesting = isTestingStreams,
                            channelCount = channels.size
                        )
                    },
                    containerColor = DarkBackground
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        when (selectedTab) {
                            0 -> ExtractScreen(viewModel = viewModel)
                            1 -> ChannelsScreen(viewModel = viewModel)
                            2 -> RawM3uScreen(viewModel = viewModel)
                            3 -> HistoryScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        } else {
            // Mobile Portrait NavigationBar Layout
            Scaffold(
                topBar = {
                    AppTopBar(
                        title = "M3U Extractor",
                        subtitle = when (selectedTab) {
                            0 -> "Ekstrak Playlist dari URL APK"
                            1 -> "${channels.size} Channel Siaran Terdeteksi"
                            2 -> "Pratinjau & Ekspor File .M3U"
                            else -> "Riwayat Playlist Tersimpan"
                        },
                        onTestAllClick = if (selectedTab == 1 && channels.isNotEmpty()) {
                            { viewModel.testAllChannels() }
                        } else null,
                        isTesting = isTestingStreams,
                        channelCount = channels.size
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("app_bottom_navigation")
                    ) {
                        navItems.forEachIndexed { index, item ->
                            val isSelected = selectedTab == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedTab(index) },
                                icon = {
                                    NavIconWithBadge(item = item, isSelected = isSelected)
                                },
                                label = {
                                    Text(
                                        item.title,
                                        fontSize = 11.sp,
                                        color = if (isSelected) CyanNeon else TextMuted
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DarkSurface,
                                    indicatorColor = CyanNeon,
                                    unselectedIconColor = TextMuted
                                ),
                                modifier = Modifier.testTag("nav_tab_$index")
                            )
                        }
                    }
                },
                containerColor = DarkBackground
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    when (selectedTab) {
                        0 -> ExtractScreen(viewModel = viewModel)
                        1 -> ChannelsScreen(viewModel = viewModel)
                        2 -> RawM3uScreen(viewModel = viewModel)
                        3 -> HistoryScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Video Player Modal
    currentlyPlayingChannel?.let { channel ->
        PlayerDialog(
            channel = channel,
            onDismiss = { viewModel.setCurrentlyPlayingChannel(null) }
        )
    }
}

@Composable
fun NavIconWithBadge(item: NavTabItem, isSelected: Boolean) {
    if (item.badgeCount > 0) {
        BadgedBox(
            badge = {
                Badge(
                    containerColor = ElectricViolet,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("${item.badgeCount}")
                }
            }
        ) {
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.title
            )
        }
    } else {
        Icon(
            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title
        )
    }
}
