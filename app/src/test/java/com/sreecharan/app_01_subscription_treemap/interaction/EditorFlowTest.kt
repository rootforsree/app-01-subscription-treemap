package com.sreecharan.app_01_subscription_treemap.interaction

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionInput
import com.sreecharan.app_01_subscription_treemap.ui.editor.SubscriptionEditorContent
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Interaction tests for the subscription editor. The inner content composable
 * is driven directly (no ViewModel, no Hilt); the ModalBottomSheet wrapper is
 * skipped because sheet window insets are meaningless under Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EditorFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    private companion object {
        const val EXPECTED_AMOUNT = 15.99
    }

    private fun render(
        onSave: (SubscriptionInput) -> Unit,
        onDelete: (Long) -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = false, hapticsEnabled = false) {
                SubscriptionEditorContent(
                    initial = null,
                    categories = SubscriptionCategory.entries,
                    onSave = onSave,
                    onDelete = onDelete,
                    onDismiss = {},
                )
            }
        }
    }

    @Test
    fun save_withValidInput_capturesSubscriptionInput() {
        var captured: SubscriptionInput? = null
        render(onSave = { captured = it })

        composeRule.onNodeWithTag("editor_name_field").performTextReplacement("HBO Max")
        composeRule.onNodeWithTag("editor_amount_field").performClick()
        composeRule.onNodeWithTag("editor_amount_field").performTextReplacement("15.99")
        composeRule.onNodeWithText("Yearly").performClick()
        composeRule.onNodeWithText("Streaming").performClick()
        composeRule.onNodeWithText("Save").performClick()

        val input = captured
        assertNotNull(input)
        assertEquals("HBO Max", input?.name)
        assertEquals(EXPECTED_AMOUNT, input?.amount ?: 0.0, 0.0)
        assertEquals(BillingCycle.YEARLY, input?.cycle)
        assertEquals(SubscriptionCategory.STREAMING, input?.category)
        assertNull(input?.id)
    }

    @Test
    fun save_withBlankName_showsErrorAndDoesNotSave() {
        var captured: SubscriptionInput? = null
        render(onSave = { captured = it })

        composeRule.onNodeWithTag("editor_amount_field").performTextReplacement("9.99")
        composeRule.onNodeWithText("Save").performClick()

        composeRule.onNodeWithText("Enter a name").assertIsDisplayed()
        assertNull(captured)
    }

    @Test
    fun save_withInvalidAmount_showsErrorAndDoesNotSave() {
        var captured: SubscriptionInput? = null
        render(onSave = { captured = it })

        composeRule.onNodeWithTag("editor_name_field").performTextReplacement("HBO Max")
        composeRule.onNodeWithTag("editor_amount_field").performTextReplacement("0")
        composeRule.onNodeWithText("Save").performClick()

        composeRule.onNodeWithText("Enter an amount above $0").assertIsDisplayed()
        assertNull(captured)
    }
}
