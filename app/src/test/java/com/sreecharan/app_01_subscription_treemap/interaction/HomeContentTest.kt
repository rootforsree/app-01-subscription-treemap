package com.sreecharan.app_01_subscription_treemap.interaction

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.treemap.TreemapTile
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeScreenContent
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeUiState
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Interaction tests for the home screen content. Fabricated [HomeUiState]
 * drives the stateless content directly; no ViewModel, no repository.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeContentTest {
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
        )

    private val sampleTiles =
        listOf(
            TreemapTile(subscriptionId = 1, x = 0f, y = 0f, width = 0.5f, height = 1f),
            TreemapTile(subscriptionId = 2, x = 0.5f, y = 0f, width = 0.5f, height = 0.5f),
            TreemapTile(subscriptionId = 3, x = 0.5f, y = 0.5f, width = 0.5f, height = 0.5f),
        ).toImmutableList()

    private fun baseState() =
        HomeUiState(
            isLoading = false,
            subscriptions = sampleSubscriptions.toImmutableList(),
            tiles = sampleTiles,
            totalMonthly = 32.97,
            totalYearly = 395.64,
            categoriesPresent =
                persistentListOf(
                    SubscriptionCategory.STREAMING,
                    SubscriptionCategory.MUSIC,
                    SubscriptionCategory.CLOUD,
                ),
            unacknowledgedHikeCount = 2,
        )

    private fun render(
        state: HomeUiState,
        onCategorySelected: (SubscriptionCategory?) -> Unit = {},
        onAddClick: () -> Unit = {},
        onSubscriptionClick: (Long) -> Unit = {},
        onRetry: () -> Unit = {},
        onNavigateToHikes: () -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = false, hapticsEnabled = false) {
                val snackbarHostState = remember { SnackbarHostState() }
                HomeScreenContent(
                    state = state,
                    onCategorySelected = onCategorySelected,
                    onAddClick = onAddClick,
                    onSubscriptionClick = onSubscriptionClick,
                    onRetry = onRetry,
                    onNavigateToHikes = onNavigateToHikes,
                    onEditorDismiss = {},
                    onDetailDismiss = {},
                    onEditFromDetail = {},
                    onSaveSubscription = {},
                    onDeleteSubscription = {},
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }

    @Test
    fun hikeBanner_visible_clickNavigatesToHikes() {
        var navigated = false
        render(baseState(), onNavigateToHikes = { navigated = true })

        composeRule.onNodeWithTag("banner_hikes").assertIsDisplayed()
        composeRule.onNodeWithText("2 price hikes detected").assertIsDisplayed()
        composeRule.onNodeWithTag("banner_hikes").performClick()

        assertTrue(navigated)
    }

    @Test
    fun categoryChip_clickSelectsCategory() {
        var selected: SubscriptionCategory? = null
        render(baseState(), onCategorySelected = { selected = it })

        composeRule.onNodeWithTag("chip_Streaming").performClick()

        assertEquals(SubscriptionCategory.STREAMING, selected)
    }

    @Test
    fun treemapTile_clickOpensDetail() {
        var clickedId: Long? = null
        render(baseState(), onSubscriptionClick = { clickedId = it })

        composeRule.onNodeWithTag("treemap_tile_1").assertIsDisplayed()
        composeRule.onNodeWithTag("treemap_tile_1").performClick()

        assertEquals(1L, clickedId)
    }

    @Test
    fun emptyState_ctaClickAddsSubscription() {
        var addClicked = false
        render(
            baseState().copy(
                subscriptions = persistentListOf(),
                tiles = persistentListOf(),
                totalMonthly = 0.0,
                totalYearly = 0.0,
                categoriesPresent = persistentListOf(),
                unacknowledgedHikeCount = 0,
            ),
            onAddClick = { addClicked = true },
        )

        composeRule.onNodeWithText("No subscriptions yet").assertIsDisplayed()
        composeRule.onNodeWithText("Add subscription").performClick()

        assertTrue(addClicked)
    }

    @Test
    fun errorState_retryClickRetries() {
        var retried = false
        render(
            baseState().copy(errorMessage = "Could not load subscriptions."),
            onRetry = { retried = true },
        )

        composeRule.onNodeWithText("Could not load subscriptions.").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertTrue(retried)
    }
}
