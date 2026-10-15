package com.sreecharan.app_01_subscription_treemap.interaction

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import com.sreecharan.app_01_subscription_treemap.ui.hikes.HikesScreenContent
import com.sreecharan.app_01_subscription_treemap.ui.hikes.HikesUiState
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Interaction tests for the price-hikes screen. Fabricated [HikesUiState]
 * drives the stateless content directly; no ViewModel, no repository.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HikesContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun fixtureHike(id: Long, name: String): PriceHike = PriceHike(
        id = id,
        subscriptionId = id,
        subscriptionName = name,
        oldAmount = 15.49,
        newAmount = 17.99,
        cycle = BillingCycle.MONTHLY,
        detectedAt = 1_760_000_000_000L,
        acknowledged = false,
    )

    private fun render(state: HikesUiState, onAcknowledge: (Long) -> Unit = {}, onAcknowledgeAll: () -> Unit = {}) {
        composeRule.setContent {
            AppTheme(darkTheme = false, hapticsEnabled = false) {
                HikesScreenContent(
                    state = state,
                    onAcknowledge = onAcknowledge,
                    onAcknowledgeAll = onAcknowledgeAll,
                )
            }
        }
    }

    @Test
    fun hikeList_showsBothHikes_acknowledgeFirst() {
        var acknowledgedId: Long? = null
        render(
            HikesUiState(
                isLoading = false,
                hikes = persistentListOf(
                    fixtureHike(1, "Netflix"),
                    fixtureHike(2, "HBO Max"),
                ),
            ),
            onAcknowledge = { acknowledgedId = it },
        )

        composeRule.onNodeWithText("Netflix").assertIsDisplayed()
        composeRule.onNodeWithText("HBO Max").assertIsDisplayed()
        composeRule.onNodeWithTag("hike_ack_1").performClick()

        assertEquals(1L, acknowledgedId)
    }

    @Test
    fun acknowledgeAll_callsOnAcknowledgeAll() {
        var acknowledgedAll = false
        render(
            HikesUiState(
                isLoading = false,
                hikes = persistentListOf(fixtureHike(1, "Netflix")),
            ),
            onAcknowledgeAll = { acknowledgedAll = true },
        )

        composeRule.onNodeWithText("Acknowledge all").performClick()

        assertEquals(true, acknowledgedAll)
    }

    @Test
    fun empty_showsEmptyTitle() {
        var acknowledgedId: Long? = null
        render(
            HikesUiState(isLoading = false, hikes = persistentListOf()),
            onAcknowledge = { acknowledgedId = it },
        )

        composeRule.onNodeWithText("No price hikes").assertIsDisplayed()
        composeRule.onNodeWithText("Your prices are holding steady.").assertIsDisplayed()
        assertNull(acknowledgedId)
    }
}
