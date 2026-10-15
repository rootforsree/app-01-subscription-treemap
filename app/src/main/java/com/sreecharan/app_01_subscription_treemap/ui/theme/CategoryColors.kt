package com.sreecharan.app_01_subscription_treemap.ui.theme

import androidx.compose.ui.graphics.Color
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory

/**
 * Fixed iOS-style palette for treemap tiles, one hue per category. These
 * colors are data ink, not chrome: the interactive accent stays #007AFF
 * everywhere, and these never tint buttons, tabs, or text actions.
 */
object CategoryColors {
    private const val STREAMING_RED = 0xFFFF3B30
    private const val MUSIC_PINK = 0xFFFF2D55
    private const val GAMING_PURPLE = 0xFFAF52DE
    private const val NEWS_ORANGE = 0xFFFF9500
    private const val PRODUCTIVITY_BLUE = 0xFF007AFF
    private const val FINANCE_GREEN = 0xFF34C759
    private const val FITNESS_TEAL = 0xFF5AC8FA
    private const val CLOUD_INDIGO = 0xFF5856D6
    private const val UTILITIES_GRAY = 0xFF8E8E93
    private const val OTHER_BROWN = 0xFFAC8E68

    fun colorFor(category: SubscriptionCategory): Color =
        when (category) {
            SubscriptionCategory.STREAMING -> Color(STREAMING_RED)
            SubscriptionCategory.MUSIC -> Color(MUSIC_PINK)
            SubscriptionCategory.GAMING -> Color(GAMING_PURPLE)
            SubscriptionCategory.NEWS -> Color(NEWS_ORANGE)
            SubscriptionCategory.PRODUCTIVITY -> Color(PRODUCTIVITY_BLUE)
            SubscriptionCategory.FINANCE -> Color(FINANCE_GREEN)
            SubscriptionCategory.FITNESS -> Color(FITNESS_TEAL)
            SubscriptionCategory.CLOUD -> Color(CLOUD_INDIGO)
            SubscriptionCategory.UTILITIES -> Color(UTILITIES_GRAY)
            SubscriptionCategory.OTHER -> Color(OTHER_BROWN)
        }
}
