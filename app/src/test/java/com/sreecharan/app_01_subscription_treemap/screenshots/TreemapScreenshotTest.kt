package com.sreecharan.app_01_subscription_treemap.screenshots

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.model.monthlyAmount
import com.sreecharan.app_01_subscription_treemap.data.treemap.squarifiedTreemap
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeScreenContent
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeUiState
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi golden tests for the home treemap screen. Every state is a
 * fabricated deterministic fixture; no ViewModel, no repository, no network.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TreemapScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun fixtureSubscription(
        id: Long,
        name: String,
        amount: Double,
        category: SubscriptionCategory,
    ): Subscription =
        Subscription(
            id = id,
            name = name,
            amount = amount,
            cycle = BillingCycle.MONTHLY,
            category = category,
            nextBillingEpochDay = 20_370L,
            notes = "",
            createdAt = 1_760_000_000_000L,
            updatedAt = 1_760_000_000_000L,
        )

    private val sampleSubscriptions =
        listOf(
            fixtureSubscription(1, "Netflix", 17.99, SubscriptionCategory.STREAMING),
            fixtureSubscription(2, "Spotify", 11.99, SubscriptionCategory.MUSIC),
            fixtureSubscription(3, "iCloud+", 2.99, SubscriptionCategory.CLOUD),
            fixtureSubscription(4, "Notion", 10.00, SubscriptionCategory.PRODUCTIVITY),
            fixtureSubscription(5, "Disney+", 13.99, SubscriptionCategory.STREAMING),
        )

    private fun stateFor(subscriptions: List<Subscription>): HomeUiState {
        val tiles =
            squarifiedTreemap(
                subscriptions.associate { it.id to it.monthlyAmount.toFloat() },
                1f,
                1f,
            ).toImmutableList()
        return HomeUiState(
            isLoading = false,
            subscriptions = subscriptions.toImmutableList(),
            tiles = tiles,
            totalMonthly = subscriptions.sumOf { it.monthlyAmount },
            totalYearly = subscriptions.sumOf { it.monthlyAmount } * 12.0,
            categoriesPresent = subscriptions.map { it.category }.distinct().toImmutableList(),
        )
    }

    @Test
    fun home_light() {
        capture(darkTheme = false, state = stateFor(sampleSubscriptions), name = "home_light")
    }

    @Test
    fun home_dark() {
        capture(darkTheme = true, state = stateFor(sampleSubscriptions), name = "home_dark")
    }

    @Test
    fun home_loading() {
        capture(darkTheme = false, state = HomeUiState(isLoading = true), name = "home_loading")
    }

    @Test
    fun home_empty() {
        capture(
            darkTheme = false,
            state = HomeUiState(isLoading = false),
            name = "home_empty",
        )
    }

    @Test
    fun home_error() {
        capture(
            darkTheme = false,
            state =
                HomeUiState(
                    isLoading = false,
                    errorMessage = "Couldn't load your subscriptions. Check your connection and try again.",
                ),
            name = "home_error",
        )
    }

    @Test
    fun home_hikes_banner() {
        capture(
            darkTheme = false,
            state = stateFor(sampleSubscriptions).copy(unacknowledgedHikeCount = 2),
            name = "home_hikes_banner",
        )
    }

    @Test
    fun home_filtered() {
        val streaming = sampleSubscriptions.filter { it.category == SubscriptionCategory.STREAMING }
        capture(
            darkTheme = false,
            state =
                stateFor(streaming).copy(
                    categoriesPresent = sampleSubscriptions.map { it.category }.distinct().toImmutableList(),
                    selectedCategory = SubscriptionCategory.STREAMING,
                ),
            name = "home_filtered",
        )
    }

    private fun capture(
        darkTheme: Boolean,
        state: HomeUiState,
        name: String,
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = darkTheme, hapticsEnabled = false) {
                val snackbarHostState = remember { SnackbarHostState() }
                HomeScreenContent(
                    state = state,
                    onCategorySelected = {},
                    onAddClick = {},
                    onSubscriptionClick = {},
                    onRetry = {},
                    onNavigateToHikes = {},
                    onEditorDismiss = {},
                    onDetailDismiss = {},
                    onEditFromDetail = {},
                    onSaveSubscription = {},
                    onDeleteSubscription = {},
                    snackbarHostState = snackbarHostState,
                )
            }
        }
        composeRule.onRoot().captureRoboImage(name)
    }
}
