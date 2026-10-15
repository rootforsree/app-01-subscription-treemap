package com.sreecharan.app_01_subscription_treemap.screenshots

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.ui.editor.SubscriptionEditorContent
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi golden tests for the subscription editor. The inner content is
 * rendered directly (not the ModalBottomSheet, which needs window insets);
 * fabricated state only.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EditorScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun editor_add_light() {
        capture(darkTheme = false, name = "editor_add_light")
    }

    @Test
    fun editor_add_dark() {
        capture(darkTheme = true, name = "editor_add_dark")
    }

    private fun capture(
        darkTheme: Boolean,
        name: String,
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = darkTheme, hapticsEnabled = false) {
                SubscriptionEditorContent(
                    initial = null,
                    categories = SubscriptionCategory.entries,
                    onSave = {},
                    onDelete = {},
                )
            }
        }
        composeRule.onRoot().captureRoboImage(name)
    }
}
