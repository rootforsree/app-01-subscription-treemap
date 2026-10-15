package com.sreecharan.app_01_subscription_treemap.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionInput
import com.sreecharan.app_01_subscription_treemap.data.model.monthlyAmount
import com.sreecharan.app_01_subscription_treemap.data.treemap.TreemapTile
import com.sreecharan.app_01_subscription_treemap.ui.components.EmptyState
import com.sreecharan.app_01_subscription_treemap.ui.components.ErrorState
import com.sreecharan.app_01_subscription_treemap.ui.components.SkeletonList
import com.sreecharan.app_01_subscription_treemap.ui.detail.SubscriptionDetailSheet
import com.sreecharan.app_01_subscription_treemap.ui.editor.SubscriptionEditorSheet
import com.sreecharan.app_01_subscription_treemap.ui.format.formatUsd
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppShapes
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTypography
import com.sreecharan.app_01_subscription_treemap.ui.theme.CategoryColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.LocalAppColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.rememberHapticTick
import kotlinx.collections.immutable.ImmutableList

private val TileMinTextWidth = 64.dp
private val TileMinTextHeight = 44.dp

/**
 * Thin stateful route: owns the ViewModel, collects with
 * collectAsStateWithLifecycle, collects the one-shot event flow for the
 * snackbar, and forwards events. The content below stays stateless.
 */
@Composable
fun HomeRoute(
    onNavigateToHikes: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    HomeScreenContent(
        state = state,
        onCategorySelected = viewModel::onCategorySelected,
        onAddClick = viewModel::onAddClick,
        onSubscriptionClick = viewModel::onSubscriptionClick,
        onRetry = viewModel::onRetry,
        onNavigateToHikes = onNavigateToHikes,
        onEditorDismiss = viewModel::onEditorDismiss,
        onDetailDismiss = viewModel::onDetailDismiss,
        onEditFromDetail = viewModel::onEditFromDetail,
        onSaveSubscription = viewModel::saveSubscription,
        onDeleteSubscription = viewModel::deleteSubscription,
        snackbarHostState = snackbarHostState,
    )
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onCategorySelected: (SubscriptionCategory?) -> Unit,
    onAddClick: () -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    onRetry: () -> Unit,
    onNavigateToHikes: () -> Unit,
    onEditorDismiss: () -> Unit,
    onDetailDismiss: () -> Unit,
    onEditFromDetail: (Subscription) -> Unit,
    onSaveSubscription: (SubscriptionInput) -> Unit,
    onDeleteSubscription: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HomeTopBar(
                scrollBehavior = scrollBehavior,
                colors = colors,
                onAddClick = onAddClick,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = colors.groupedBackground,
    ) { padding ->
        HomeScaffoldContent(
            state = state,
            padding = padding,
            onCategorySelected = onCategorySelected,
            onSubscriptionClick = onSubscriptionClick,
            onNavigateToHikes = onNavigateToHikes,
            onAddClick = onAddClick,
            onRetry = onRetry,
        )
    }

    if (state.showAddSheet || state.editingSubscription != null) {
        SubscriptionEditorSheet(
            initial = state.editingSubscription,
            categories = SubscriptionCategory.entries,
            onSave = onSaveSubscription,
            onDelete = onDeleteSubscription,
            onDismiss = onEditorDismiss,
        )
    }
    val detailSubscription = state.detailSubscription
    if (detailSubscription != null) {
        SubscriptionDetailSheet(
            subscription = detailSubscription,
            latestHike = state.detailLatestHike,
            onEdit = { onEditFromDetail(detailSubscription) },
            onDelete = onDeleteSubscription,
            onDismiss = onDetailDismiss,
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun HomeScaffoldContent(
    state: HomeUiState,
    padding: PaddingValues,
    onCategorySelected: (SubscriptionCategory?) -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    onNavigateToHikes: () -> Unit,
    onAddClick: () -> Unit,
    onRetry: () -> Unit,
) {
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

        state.errorMessage != null -> {
            ErrorState(
                message = state.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.padding(bottom = AppDimens.tabBarClearance),
            )
        }

        state.subscriptions.isEmpty() && state.selectedCategory == null -> {
            EmptyState(
                title = stringResource(R.string.empty_title),
                message = stringResource(R.string.empty_message),
                actionLabel = stringResource(R.string.action_add_subscription),
                onAction = onAddClick,
                icon = Icons.Outlined.Dashboard,
                modifier = Modifier.padding(bottom = AppDimens.tabBarClearance),
            )
        }

        else -> {
            HomeContentBody(
                state = state,
                onCategorySelected = onCategorySelected,
                onSubscriptionClick = onSubscriptionClick,
                onNavigateToHikes = onNavigateToHikes,
                contentPadding = padding,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    colors: AppColors,
    onAddClick: () -> Unit,
) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.home_title),
                style = AppTypography.largeTitle,
            )
        },
        actions = {
            IconButton(onClick = onAddClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.action_add_subscription),
                    tint = colors.accent,
                )
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

@Composable
private fun HomeContentBody(
    state: HomeUiState,
    onCategorySelected: (SubscriptionCategory?) -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    onNavigateToHikes: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(colors.groupedBackground)
                .verticalScroll(rememberScrollState())
                .padding(
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + AppDimens.tabBarClearance,
                ).padding(horizontal = AppDimens.screenMargin),
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing16),
    ) {
        SummaryHeader(
            totalMonthly = state.totalMonthly,
            totalYearly = state.totalYearly,
        )
        if (state.unacknowledgedHikeCount > 0) {
            HikeBanner(
                count = state.unacknowledgedHikeCount,
                onClick = onNavigateToHikes,
            )
        }
        CategoryChipsRow(
            categories = state.categoriesPresent,
            selected = state.selectedCategory,
            onSelect = onCategorySelected,
        )
        if (state.subscriptions.isEmpty()) {
            Text(
                text = stringResource(R.string.empty_filtered),
                style = AppTypography.body,
                color = colors.secondaryLabel,
                modifier = Modifier.padding(vertical = AppDimens.spacing24),
            )
        } else {
            TreemapCard(
                tiles = state.tiles,
                subscriptions = state.subscriptions,
                onTileClick = onSubscriptionClick,
            )
        }
    }
}

@Composable
private fun SummaryHeader(
    totalMonthly: Double,
    totalYearly: Double,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppDimens.spacing4),
    ) {
        Text(
            text = stringResource(R.string.summary_per_month),
            style = AppTypography.subheadline,
            color = colors.secondaryLabel,
        )
        Text(
            text = formatUsd(totalMonthly),
            style = AppTypography.largeTitle,
            color = colors.label,
        )
        Text(
            text = stringResource(R.string.summary_per_year, formatUsd(totalYearly)),
            style = AppTypography.subheadline,
            color = colors.secondaryLabel,
        )
    }
}

@Composable
private fun HikeBanner(
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShapes.card)
                .background(colors.warning.copy(alpha = 0.15f))
                .clickable(onClick = onClick)
                .testTag("banner_hikes")
                .padding(AppDimens.spacing16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing12),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            tint = colors.warning,
            modifier = Modifier.size(AppDimens.iconSize),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.hikes_banner, count),
                style = AppTypography.headline,
                color = colors.label,
            )
            Text(
                text = stringResource(R.string.hikes_banner_cta),
                style = AppTypography.footnote,
                color = colors.accent,
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = colors.secondaryLabel,
            modifier = Modifier.size(AppDimens.iconSize),
        )
    }
}

@Composable
private fun CategoryChipsRow(
    categories: ImmutableList<SubscriptionCategory>,
    selected: SubscriptionCategory?,
    onSelect: (SubscriptionCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacing8),
    ) {
        CategoryChip(
            label = stringResource(R.string.filter_all),
            selected = selected == null,
            onClick = { onSelect(null) },
            testTag = "chip_All",
        )
        for (category in categories) {
            CategoryChip(
                label = category.displayName,
                selected = selected == category,
                onClick = { onSelect(category) },
                testTag = "chip_${category.displayName}",
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tick = rememberHapticTick()
    Box(
        modifier =
            modifier
                .height(AppDimens.chipHeight)
                .clip(AppShapes.capsule)
                .background(if (selected) colors.accent else colors.cardSurface)
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = {
                        tick()
                        onClick()
                    },
                ).testTag(testTag)
                .padding(horizontal = AppDimens.spacing16),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = AppTypography.subheadline,
            color = if (selected) Color.White else colors.label,
            maxLines = 1,
        )
    }
}

/**
 * The treemap itself: each tile sits at its fractional offset/size, inset by
 * 2dp on every side so 4dp gaps open between tiles. Tiles too small for text
 * stay plain color blocks.
 */
@Composable
private fun TreemapCard(
    tiles: ImmutableList<TreemapTile>,
    subscriptions: ImmutableList<Subscription>,
    onTileClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val byId = remember(subscriptions) { subscriptions.associateBy { it.id } }
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(AppShapes.card)
                .background(colors.cardSurface),
    ) {
        for (tile in tiles) {
            val subscription = byId[tile.subscriptionId] ?: continue
            TreemapTile(
                tile = tile,
                subscription = subscription,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                onTileClick = onTileClick,
            )
        }
    }
}

@Composable
private fun BoxWithConstraintsScope.TreemapTile(
    tile: TreemapTile,
    subscription: Subscription,
    maxWidth: Dp,
    maxHeight: Dp,
    onTileClick: (Long) -> Unit,
) {
    val tileWidth = maxWidth * tile.width
    val tileHeight = maxHeight * tile.height
    Box(
        modifier =
            Modifier
                .offset(x = maxWidth * tile.x, y = maxHeight * tile.y)
                .size(width = tileWidth, height = tileHeight)
                .clip(RoundedCornerShape(6.dp))
                .testTag("treemap_tile_${tile.subscriptionId}")
                .semantics(mergeDescendants = true) {
                    contentDescription = subscription.name
                }.clickable { onTileClick(tile.subscriptionId) },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CategoryColors.colorFor(subscription.category))
                    .padding(AppDimens.spacing8),
            contentAlignment = Alignment.TopStart,
        ) {
            if (tileWidth >= TileMinTextWidth && tileHeight >= TileMinTextHeight) {
                Column {
                    Text(
                        text = subscription.name,
                        style = AppTypography.headline,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.per_month_short,
                                formatUsd(subscription.monthlyAmount),
                            ),
                        style = AppTypography.footnote,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
