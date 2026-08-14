package com.pronoagg.aggregator.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pronoagg.aggregator.ui.channels.ManageChannelsScreen
import com.pronoagg.aggregator.ui.feed.PronoFeedScreen

private object Routes {
    const val FEED = "feed"
    const val CHANNELS = "channels"
}

@Composable
fun PronoAggApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.FEED) {
        composable(Routes.FEED) {
            PronoFeedScreen(onOpenChannels = { navController.navigate(Routes.CHANNELS) })
        }
        composable(Routes.CHANNELS) {
            ManageChannelsScreen(onBack = { navController.popBackStack() })
        }
    }
}
