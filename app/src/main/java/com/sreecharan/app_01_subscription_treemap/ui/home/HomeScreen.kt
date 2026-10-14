package com.sreecharan.app_01_subscription_treemap.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.data.model.Note
import com.sreecharan.app_01_subscription_treemap.ui.components.EmptyState
import com.sreecharan.app_01_subscription_treemap.ui.components.ErrorState
import com.sreecharan.app_01_subscription_treemap.ui.components.GroupCard
import com.sreecharan.app_01_subscription_treemap.ui.components.InsetGroupedList
import com.sreecharan.app_01_subscription_treemap.ui.components.SkeletonList
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTypography
import com.sreecharan.app_01_subscription_treemap.ui.theme.LocalAppColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.rememberHapticTick
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Thin stateful route: owns the ViewModel, collects with
 * collectAsStateWithLifecycle, and forwards events. The content below stays
 * stateless and never sees the ViewModel.
 */
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        state = state,
        onRefresh = viewModel::onRefresh,
        onRetry = viewModel::onRetry,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick(medium = true)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = AppTypography.largeTitle,
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = colors.groupedBackground,
                    scrolledContainerColor = colors.groupedBackground,
                ),
            )
        },
        containerColor = colors.groupedBackground,
    ) { padding ->
        if (state.isLoading) {
            SkeletonList(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + AppDimens.tabBarClearance,
                ),
            )
        } else {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = {
                    tick()
                    onRefresh()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.groupedBackground)
                    .padding(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding(),
                    ),
            ) {
                val message = state.errorMessage
                when {
                    message != null -> ErrorState(
                        message = message,
                        onRetry = onRetry,
                        modifier = Modifier.padding(bottom = AppDimens.tabBarClearance),
                    )
                    state.notes.isEmpty() -> EmptyState(
                        message = stringResource(R.string.home_empty_message),
                        actionLabel = stringResource(R.string.action_refresh),
                        onAction = onRefresh,
                        icon = Icons.Outlined.Description,
                        modifier = Modifier.padding(bottom = AppDimens.tabBarClearance),
                    )
                    else -> InsetGroupedList(
                        contentPadding = PaddingValues(bottom = AppDimens.tabBarClearance),
                    ) {
                        items(
                            items = state.notes,
                            key = { note -> note.id },
                            contentType = { "note" },
                        ) { note ->
                            GroupCard {
                                NoteRow(note = note)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteRow(
    note: Note,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier.padding(AppDimens.spacing16),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4),
    ) {
        Text(
            text = note.title,
            style = AppTypography.headline,
            color = colors.label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = note.body,
            style = AppTypography.subheadline,
            color = colors.secondaryLabel,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = formatNoteDate(note.createdAt),
            style = AppTypography.caption,
            color = colors.secondaryLabel,
        )
    }
}

private fun formatNoteDate(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()))
