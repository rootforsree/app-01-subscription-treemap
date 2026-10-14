package com.sreecharan.app_01_subscription_treemap.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.ui.about.AboutRoute
import com.sreecharan.app_01_subscription_treemap.ui.components.GlassTabBar
import com.sreecharan.app_01_subscription_treemap.ui.components.TabItem
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeRoute
import com.sreecharan.app_01_subscription_treemap.ui.settings.SettingsRoute
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object SettingsRoute

@Serializable
object AboutRoute

/**
 * One activity, one root NavHost, type-safe routes. The glass tab bar floats
 * over the NavHost content; per-tab state survives tab switches through
 * saveState/restoreState, and screens reserve bottom clearance for the bar.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val tabs = persistentListOf(
        TabItem(
            label = stringResource(R.string.tab_home),
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        TabItem(
            label = stringResource(R.string.tab_settings),
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        ),
        TabItem(
            label = stringResource(R.string.tab_about),
            selectedIcon = Icons.Filled.Info,
            unselectedIcon = Icons.Outlined.Info,
        ),
    )
    val selectedIndex = when {
        destination?.hasRoute(HomeRoute::class) == true -> 0
        destination?.hasRoute(SettingsRoute::class) == true -> 1
        destination?.hasRoute(AboutRoute::class) == true -> 2
        else -> 0
    }
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<HomeRoute> { HomeRoute() }
            composable<SettingsRoute> { SettingsRoute() }
            composable<AboutRoute> { AboutRoute() }
        }
        GlassTabBar(
            tabs = tabs,
            selectedIndex = selectedIndex,
            onTabSelected = { index ->
                val route: Any = when (index) {
                    0 -> HomeRoute
                    1 -> SettingsRoute
                    else -> AboutRoute
                }
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(
                    start = AppDimens.screenMargin,
                    end = AppDimens.screenMargin,
                    bottom = AppDimens.screenMargin,
                ),
        )
    }
}
