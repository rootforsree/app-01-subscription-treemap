package com.sreecharan.app_01_subscription_treemap.ui.theme

import androidx.compose.ui.graphics.Color
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory

/**
 * Fixed iOS-style palette for treemap tiles, one hue per category. These
 * colors are data ink, not chrome: the interactive accent stays #007AFF
 * everywhere, and these never tint buttons, tabs, or text actions.
 */
object CategoryColors {
    fun colorFor(category: SubscriptionCategory): Color = when (category) {
        SubscriptionCategory.STREAMING -> Color(0xFFFF3B30)
        SubscriptionCategory.MUSIC -> Color(0xFFFF2D55)
        SubscriptionCategory.GAMING -> Color(0xFFAF52DE)
        SubscriptionCategory.NEWS -> Color(0xFFFF9500)
        SubscriptionCategory.PRODUCTIVITY -> Color(0xFF007AFF)
        SubscriptionCategory.FINANCE -> Color(0xFF34C759)
        SubscriptionCategory.FITNESS -> Color(0xFF5AC8FA)
        SubscriptionCategory.CLOUD -> Color(0xFF5856D6)
        SubscriptionCategory.UTILITIES -> Color(0xFF8E8E93)
        SubscriptionCategory.OTHER -> Color(0xFFAC8E68)
    }
}
