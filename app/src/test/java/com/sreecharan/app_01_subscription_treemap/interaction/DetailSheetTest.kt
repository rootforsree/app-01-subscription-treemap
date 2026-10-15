package com.sreecharan.app_01_subscription_treemap.interaction

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.ui.detail.SubscriptionDetailContent
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Interaction tests for the subscription detail sheet. The inner content
 * composable is driven directly; the ModalBottomSheet wrapper is skipped
 * because sheet window insets are meaningless under Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    private companion object {
        const val SUBSCRIPTION_ID = 7L
    }

    private val subscription =
        Subscription(
            id = SUBSCRIPTION_ID,
            name = "Netflix",
            amount = 17.99,
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.STREAMING,
            nextBillingEpochDay = 20_370L,
            notes = "Standard plan.",
            createdAt = 1_760_000_000_000L,
            updatedAt = 1_760_000_000_000L,
        )

    private fun render(
        onEdit: () -> Unit = {},
        onDelete: (Long) -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = false, hapticsEnabled = false) {
                SubscriptionDetailContent(
                    subscription = subscription,
                    latestHike = null,
                    onEdit = onEdit,
                    onDelete = onDelete,
                )
            }
        }
    }

    @Test
    fun delete_asksForConfirmation_thenDeletes() {
        var deletedId: Long? = null
        render(onDelete = { deletedId = it })

        composeRule.onNodeWithTag("detail_delete_button").performClick()
        composeRule.onNodeWithText("Delete Netflix?").assertIsDisplayed()
        composeRule.onNodeWithTag("delete_confirm_button").performClick()

        assertEquals(SUBSCRIPTION_ID, deletedId)
    }

    @Test
    fun delete_cancelled_doesNotDelete() {
        var deletedId: Long? = null
        render(onDelete = { deletedId = it })

        composeRule.onNodeWithTag("detail_delete_button").performClick()
        composeRule.onNodeWithText("Cancel").performClick()

        assertNull(deletedId)
    }

    @Test
    fun edit_callsOnEdit() {
        var edited = false
        render(onEdit = { edited = true })

        composeRule.onNodeWithText("Edit").performClick()

        assertTrue(edited)
    }
}
