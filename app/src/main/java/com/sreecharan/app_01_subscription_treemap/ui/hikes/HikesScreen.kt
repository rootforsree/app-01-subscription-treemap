package com.sreecharan.app_01_subscription_treemap.ui.hikes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import com.sreecharan.app_01_subscription_treemap.data.model.newMonthly
import com.sreecharan.app_01_subscription_treemap.data.model.oldMonthly
import com.sreecharan.app_01_subscription_treemap.data.model.percentIncrease
import com.sreecharan.app_01_subscription_treemap.ui.components.EmptyState
import com.sreecharan.app_01_subscription_treemap.ui.components.GroupCard
import com.sreecharan.app_01_subscription_treemap.ui.components.InsetGroupedList
import com.sreecharan.app_01_subscription_treemap.ui.components.RowDivider
import com.sreecharan.app_01_subscription_treemap.ui.components.SkeletonList
import com.sreecharan.app_01_subscription_treemap.ui.format.formatEpochMillisShort
import com.sreecharan.app_01_subscription_treemap.ui.format.formatPercentOneDecimal
import com.sreecharan.app_01_subscription_treemap.ui.format.formatUsd
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTypography
import com.sreecharan.app_01_subscription_treemap.ui.theme.LocalAppColors

/**
 * Thin stateful route: owns the ViewModel and forwards events. The content
 * below stays stateless and never sees the ViewModel.
 */
@Composable
fun HikesRoute(viewModel: HikesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HikesScreenContent(
        state = state,
        onAcknowledge = viewModel::onAcknowledge,
        onAcknowledgeAll = viewModel::onAcknowledgeAll,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikesScreenContent(
    state: HikesUiState,
    onAcknowledge: (Long) -> Unit,
    onAcknowledgeAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HikesTopBar(
                scrollBehavior = scrollBehavior,
                colors = colors,
                showAcknowledgeAll = state.hikes.isNotEmpty(),
                onAcknowledgeAll = onAcknowledgeAll,
            )
        },
        containerColor = colors.groupedBackground,
    ) { padding ->
        when {
            state.isLoading -> {
                SkeletonList(
                    contentPadding =
                        PaddingValues(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding() + AppDimens.tabBarClearance,
                        ),
                )
            }

            state.hikes.isEmpty() -> {
                EmptyState(
                    title = stringResource(R.string.hikes_empty_title),
                    message = stringResource(R.string.hikes_empty_message),
                    icon = Icons.Outlined.CheckCircle,
                    modifier = Modifier.padding(bottom = AppDimens.tabBarClearance),
                )
            }

            else -> {
                InsetGroupedList(
                    contentPadding =
                        PaddingValues(
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding() + AppDimens.tabBarClearance,
                        ),
                ) {
                    item(key = "hikes", contentType = "hikes") {
                        GroupCard {
                            state.hikes.forEachIndexed { index, hike ->
                                if (index > 0) {
                                    RowDivider()
                                }
                                HikeRow(
                                    hike = hike,
                                    onAcknowledge = onAcknowledge,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HikeRow(
    hike: PriceHike,
    onAcknowledge: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppDimens.rowHorizontalPadding,
                    vertical = AppDimens.rowVerticalPadding,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4),
        ) {
            Text(
                text = hike.subscriptionName,
                style = AppTypography.headline,
                color = colors.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val amounts =
                stringResource(
                    R.string.hike_row,
                    formatUsd(hike.oldMonthly),
                    formatUsd(hike.newMonthly),
                    formatPercentOneDecimal(hike.percentIncrease),
                )
            val detected =
                stringResource(
                    R.string.hike_detected,
                    formatEpochMillisShort(hike.detectedAt),
                )
            Text(
                text = "$amounts · $detected",
                style = AppTypography.footnote,
                color = colors.secondaryLabel,
            )
        }
        TextButton(
            onClick = { onAcknowledge(hike.id) },
            modifier = Modifier.testTag("hike_ack_${hike.id}"),
        ) {
            Text(
                text = stringResource(R.string.action_acknowledge),
                color = colors.accent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HikesTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    colors: AppColors,
    showAcknowledgeAll: Boolean,
    onAcknowledgeAll: () -> Unit,
) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.hikes_title),
                style = AppTypography.largeTitle,
            )
        },
        actions = {
            if (showAcknowledgeAll) {
                TextButton(onClick = onAcknowledgeAll) {
                    Text(
                        text = stringResource(R.string.action_acknowledge_all),
                        color = colors.accent,
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = colors.groupedBackground,
                scrolledContainerColor = colors.groupedBackground,
            ),
    )
}
