package com.sreecharan.app_01_subscription_treemap.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.ui.detail.SubscriptionDetailContent
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi golden test for the subscription detail sheet. The inner content
 * is rendered directly (not the ModalBottomSheet, which needs window insets);
 * fabricated state only.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun detail_light() {
        composeRule.setContent {
            AppTheme(darkTheme = false, hapticsEnabled = false) {
                SubscriptionDetailContent(
                    subscription =
                        Subscription(
                            id = 1,
                            name = "Netflix",
                            amount = 17.99,
                            cycle = BillingCycle.MONTHLY,
                            category = SubscriptionCategory.STREAMING,
                            nextBillingEpochDay = 20_370L,
                            notes = "Standard plan.",
                            createdAt = 1_760_000_000_000L,
                            updatedAt = 1_760_000_000_000L,
                        ),
                    latestHike =
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
                    onEdit = {},
                    onDelete = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage("detail_light")
    }
}
