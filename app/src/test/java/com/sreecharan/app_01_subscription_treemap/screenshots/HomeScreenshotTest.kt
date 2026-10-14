package com.sreecharan.app_01_subscription_treemap.screenshots

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.captureRoboImage
import com.sreecharan.app_01_subscription_treemap.data.model.Note
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeScreenContent // ASSUMPTION: UI worker places this in ui.home
import com.sreecharan.app_01_subscription_treemap.ui.home.HomeUiState
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

/**
 * Roborazzi golden tests for the home screen.
 * All states are fabricated deterministic fixtures; no repository, no network.
 * Verified against Roborazzi 1.70.0 API (pinned in libs.versions.toml) (io.github.takahirom.roborazzi).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val sampleNotes = persistentListOf(
        Note(id = 1, title = "Grocery list", body = "Eggs, milk, coffee", createdAt = 1_700_000_000_000),
        Note(id = 2, title = "Ideas", body = "Build the thing", createdAt = 1_700_000_001_000),
        Note(id = 3, title = "Reminder", body = "Call mom", createdAt = 1_700_000_002_000),
    )

    @Test
    fun home_light() {
        capture(
            darkTheme = false,
            state = HomeUiState(isLoading = false, notes = sampleNotes),
            name = "home_light",
        )
    }

    @Test
    fun home_dark() {
        capture(
            darkTheme = true,
            state = HomeUiState(isLoading = false, notes = sampleNotes),
            name = "home_dark",
        )
    }

    @Test
    fun home_loading() {
        capture(
            darkTheme = false,
            state = HomeUiState(isLoading = true),
            name = "home_loading",
        )
    }

    @Test
    fun home_empty() {
        capture(
            darkTheme = false,
            state = HomeUiState(isLoading = false, notes = persistentListOf()),
            name = "home_empty",
        )
    }

    @Test
    fun home_error() {
        capture(
            darkTheme = false,
            state = HomeUiState(
                isLoading = false,
                notes = persistentListOf(),
                errorMessage = "Couldn't load notes. Check your connection and try again.",
            ),
            name = "home_error",
        )
    }

    private fun capture(darkTheme: Boolean, state: HomeUiState, name: String) {
        composeRule.setContent {
            AppTheme(darkTheme = darkTheme, hapticsEnabled = false) {
                HomeScreenContent(
                    state = state,
                    onRefresh = {},
                    onRetry = {},
                )
            }
        }
        composeRule.captureRoboImage(name)
    }
}
