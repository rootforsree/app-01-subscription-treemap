package com.sreecharan.app_01_subscription_treemap.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
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
import com.sreecharan.app_01_subscription_treemap.ui.hikes.HikesRoute
import com.sreecharan.app_01_subscription_treemap.ui.hikes.HikesViewModel
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeRoute
import com.sreecharan.app_01_subscription_treemap.ui.settings.SettingsRoute
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
object HikesRoute

@Serializable
object SettingsRoute

@Serializable
object AboutRoute

/**
 * One activity, one root NavHost, type-safe routes. The glass tab bar floats
 * over the NavHost content; per-tab state survives tab switches through
 * saveState/restoreState, and screens reserve bottom clearance for the bar.
 * The Price Hikes tab carries the unacknowledged count in its label.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val hikesViewModel: HikesViewModel = hiltViewModel()
    val hikesState by hikesViewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val hikeCount = hikesState.hikes.size
    val hikesLabel = if (hikeCount > 0) {
        stringResource(R.string.tab_hikes_badge, hikeCount)
    } else {
        stringResource(R.string.tab_hikes)
    }
    val tabs = rememberTabs(hikesLabel = hikesLabel)
    val selectedIndex = when {
        destination?.hasRoute(HomeRoute::class) == true -> 0
        destination?.hasRoute(HikesRoute::class) == true -> 1
        destination?.hasRoute(SettingsRoute::class) == true -> 2
        destination?.hasRoute(AboutRoute::class) == true -> 3
        else -> 0
    }
    Box(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable<HomeRoute> {
                HomeRoute(onNavigateToHikes = { navController.navigateTab(HikesRoute) })
            }
            composable<HikesRoute> {
                HikesRoute(viewModel = hikesViewModel)
            }
            composable<SettingsRoute> { SettingsRoute() }
            composable<AboutRoute> { AboutRoute() }
        }
        GlassTabBar(
            tabs = tabs,
            selectedIndex = selectedIndex,
            onTabSelected = { index ->
                val route: Any = when (index) {
                    0 -> HomeRoute
                    1 -> HikesRoute
                    2 -> SettingsRoute
                    else -> AboutRoute
                }
                navController.navigateTab(route)
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

private fun NavController.navigateTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun rememberTabs(hikesLabel: String) = persistentListOf(
    TabItem(
        label = stringResource(R.string.tab_subscriptions),
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
    ),
    TabItem(
        label = hikesLabel,
        selectedIcon = Icons.Filled.TrendingUp,
        unselectedIcon = Icons.Outlined.TrendingUp,
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
