package com.sreecharan.app_01_subscription_treemap.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.monthlyAmount
import com.sreecharan.app_01_subscription_treemap.data.model.newMonthly
import com.sreecharan.app_01_subscription_treemap.data.model.oldMonthly
import com.sreecharan.app_01_subscription_treemap.data.model.percentIncrease
import com.sreecharan.app_01_subscription_treemap.data.model.yearlyAmount
import com.sreecharan.app_01_subscription_treemap.ui.components.GroupCard
import com.sreecharan.app_01_subscription_treemap.ui.components.RowDivider
import com.sreecharan.app_01_subscription_treemap.ui.format.formatEpochDay
import com.sreecharan.app_01_subscription_treemap.ui.format.formatPercentOneDecimal
import com.sreecharan.app_01_subscription_treemap.ui.format.formatUsd
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppShapes
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTypography
import com.sreecharan.app_01_subscription_treemap.ui.theme.CategoryColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.LocalAppColors

/**
 * Read-only subscription detail with Edit and Delete actions. Deletion is
 * destructive, so it asks for confirmation in an alert first.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailSheet(
    subscription: Subscription,
    latestHike: PriceHike?,
    onEdit: () -> Unit,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = AppShapes.sheet,
        containerColor = LocalAppColors.current.cardSurface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        SubscriptionDetailContent(
            subscription = subscription,
            latestHike = latestHike,
            onEdit = onEdit,
            onDelete = onDelete,
        )
    }
}

@Composable
@Suppress("LongMethod") // Detail sheet: header, stats, hike alert, actions; cohesive as one unit
fun SubscriptionDetailContent(
    subscription: Subscription,
    latestHike: PriceHike?,
    onEdit: () -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    val cycleNoun =
        when (subscription.cycle) {
            BillingCycle.WEEKLY -> stringResource(R.string.noun_week)
            BillingCycle.MONTHLY -> stringResource(R.string.noun_month)
            BillingCycle.QUARTERLY -> stringResource(R.string.noun_quarter)
            BillingCycle.YEARLY -> stringResource(R.string.noun_year)
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.spacing16)
                .padding(bottom = AppDimens.spacing24),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing16),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(AppDimens.spacing12)
                        .clip(CircleShape)
                        .background(CategoryColors.colorFor(subscription.category)),
            )
            Text(
                text = subscription.category.displayName,
                style = AppTypography.footnote,
                color = colors.secondaryLabel,
            )
        }
        Text(
            text = subscription.name,
            style = AppTypography.title2,
            color = colors.label,
        )
        if (latestHike != null) {
            HikeWarningRow(hike = latestHike)
        }
        GroupCard {
            DetailRow(
                label = stringResource(R.string.detail_cost),
                value =
                    stringResource(
                        R.string.detail_cost_value,
                        formatUsd(subscription.amount),
                        cycleNoun,
                    ),
            )
            RowDivider()
            DetailRow(
                label = stringResource(R.string.detail_monthly),
                value = formatUsd(subscription.monthlyAmount),
            )
            RowDivider()
            DetailRow(
                label = stringResource(R.string.detail_yearly),
                value = formatUsd(subscription.yearlyAmount),
            )
            RowDivider()
            DetailRow(
                label = stringResource(R.string.detail_next_billing),
                value =
                    subscription.nextBillingEpochDay?.let { formatEpochDay(it) }
                        ?: stringResource(R.string.date_none),
            )
            if (subscription.notes.isNotBlank()) {
                RowDivider()
                DetailRow(
                    label = stringResource(R.string.detail_notes),
                    value = subscription.notes,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
        ) {
            Button(
                onClick = onEdit,
                shape = AppShapes.capsule,
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = AppDimens.rowMinHeight),
            ) {
                Text(
                    text = stringResource(R.string.action_edit),
                    style = AppTypography.headline,
                )
            }
            TextButton(
                onClick = { showDeleteConfirm = true },
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = AppDimens.rowMinHeight)
                        .testTag("detail_delete_button"),
            ) {
                Text(
                    text = stringResource(R.string.action_delete),
                    style = AppTypography.headline,
                    color = colors.destructive,
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_title, subscription.name)) },
            text = { Text(stringResource(R.string.delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(subscription.id)
                    },
                    modifier = Modifier.testTag("delete_confirm_button"),
                ) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = colors.destructive,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.spacing16,
                    vertical = AppDimens.spacing12,
                ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = AppTypography.body,
            color = colors.secondaryLabel,
        )
        Spacer(modifier = Modifier.width(AppDimens.spacing16))
        Text(
            text = value,
            style = AppTypography.headline,
            color = colors.label,
        )
    }
}

@Composable
private fun HikeWarningRow(
    hike: PriceHike,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShapes.card)
                .background(colors.warning.copy(alpha = 0.15f))
                .padding(AppDimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = colors.warning,
            modifier = Modifier.size(AppDimens.iconSize),
        )
        Text(
            text =
                stringResource(
                    R.string.detail_hike_increase,
                    formatUsd(hike.oldMonthly),
                    formatUsd(hike.newMonthly),
                    formatPercentOneDecimal(hike.percentIncrease),
                ),
            style = AppTypography.subheadline,
            color = colors.label,
        )
    }
}
