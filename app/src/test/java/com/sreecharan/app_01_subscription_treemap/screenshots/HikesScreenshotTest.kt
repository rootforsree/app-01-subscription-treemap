package com.sreecharan.app_01_subscription_treemap.screenshots

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import com.sreecharan.app_01_subscription_treemap.ui.hikes.HikesScreenContent
import com.sreecharan.app_01_subscription_treemap.ui.hikes.HikesUiState
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi golden tests for the price-hikes screen. Fabricated [HikesUiState]
 * drives the stateless content directly; no ViewModel, no repository.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HikesScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val sampleHikes =
        persistentListOf(
            PriceHike(
                id = 1,
                subscriptionId = 1,
                subscriptionName = "Netflix",
                oldAmount = 15.49,
                newAmount = 17.99,
                cycle = BillingCycle.MONTHLY,
                detectedAt = 1_760_000_000_000L,
                acknowledged = false,
            ),
            PriceHike(
                id = 2,
                subscriptionId = 4,
                subscriptionName = "HBO Max",
                oldAmount = 15.99,
                newAmount = 16.99,
                cycle = BillingCycle.MONTHLY,
                detectedAt = 1_759_900_000_000L,
                acknowledged = false,
            ),
        )

    @Test
    fun hikes_light() {
        capture(
            darkTheme = false,
            state = HikesUiState(isLoading = false, hikes = sampleHikes),
            name = "hikes_light",
        )
    }

    @Test
    fun hikes_dark() {
        capture(
            darkTheme = true,
            state = HikesUiState(isLoading = false, hikes = sampleHikes),
            name = "hikes_dark",
        )
    }

    @Test
    fun hikes_empty() {
        capture(
            darkTheme = false,
            state = HikesUiState(isLoading = false, hikes = persistentListOf()),
            name = "hikes_empty",
        )
    }

    private fun capture(
        darkTheme: Boolean,
        state: HikesUiState,
        name: String,
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = darkTheme, hapticsEnabled = false) {
                HikesScreenContent(
                    state = state,
                    onAcknowledge = {},
                    onAcknowledgeAll = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage(name)
    }
}
