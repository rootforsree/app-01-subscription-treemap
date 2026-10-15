package com.sreecharan.app_01_subscription_treemap.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppShapes
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTypography
import com.sreecharan.app_01_subscription_treemap.ui.theme.LocalAppColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.rememberHapticTick

/**
 * Designed empty state: one icon, one line of explanation, one action.
 * The icon is decorative; the button carries the TalkBack label. Title and
 * action are optional so purely informational empty states stay clean.
 */
@Composable
fun EmptyState(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(AppDimens.spacing32),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.secondaryLabel,
            modifier = Modifier.size(AppDimens.emptyStateIcon),
        )
        Spacer(modifier = Modifier.height(AppDimens.spacing16))
        if (title != null) {
            Text(
                text = title,
                style = AppTypography.headline,
                color = colors.label,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(AppDimens.spacing8))
        }
        Text(
            text = message,
            style = AppTypography.body,
            color = colors.secondaryLabel,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(AppDimens.spacing16))
            Button(
                onClick = {
                    tick()
                    onAction()
                },
                shape = AppShapes.capsule,
                modifier = Modifier.heightIn(min = AppDimens.rowMinHeight),
            ) {
                Text(
                    text = actionLabel,
                    style = AppTypography.headline,
                )
            }
        }
    }
}
