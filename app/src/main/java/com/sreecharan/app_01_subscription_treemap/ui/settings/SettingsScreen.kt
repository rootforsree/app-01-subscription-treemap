package com.sreecharan.app_01_subscription_treemap.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings as SystemSettings
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.data.model.ThemeMode
import com.sreecharan.app_01_subscription_treemap.ui.components.GroupCard
import com.sreecharan.app_01_subscription_treemap.ui.components.InsetGroupedList
import com.sreecharan.app_01_subscription_treemap.ui.components.RowDivider
import com.sreecharan.app_01_subscription_treemap.ui.components.SectionHeader
import com.sreecharan.app_01_subscription_treemap.ui.components.SettingsRow
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppDimens
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppTypography
import com.sreecharan.app_01_subscription_treemap.ui.theme.LocalAppColors
import com.sreecharan.app_01_subscription_treemap.ui.theme.AppColors
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    SettingsScreenContent(
        state = state,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onHapticsToggled = viewModel::onHapticsToggled,
        onOpenNotificationSettings = { openNotificationSettings(context) },
    )
}

private data class ThemeOption(
    val mode: ThemeMode,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
)

private val ThemeOptions = persistentListOf(
    ThemeOption(
        mode = ThemeMode.SYSTEM,
        titleRes = R.string.theme_system,
        subtitleRes = R.string.theme_system_sub,
        icon = Icons.Filled.SettingsBrightness,
    ),
    ThemeOption(
        mode = ThemeMode.LIGHT,
        titleRes = R.string.theme_light,
        subtitleRes = R.string.theme_light_sub,
        icon = Icons.Filled.LightMode,
    ),
    ThemeOption(
        mode = ThemeMode.DARK,
        titleRes = R.string.theme_dark,
        subtitleRes = R.string.theme_dark_sub,
        icon = Icons.Filled.DarkMode,
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    state: SettingsUiState,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SettingsTopBar(scrollBehavior = scrollBehavior, colors = colors) },
        containerColor = colors.groupedBackground,
    ) { padding ->
        InsetGroupedList(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + AppDimens.tabBarClearance,
            ),
        ) {
            appearanceSection(
                state = state,
                onThemeModeSelected = onThemeModeSelected,
            )
            generalSection(
                state = state,
                onHapticsToggled = onHapticsToggled,
                onOpenNotificationSettings = onOpenNotificationSettings,
            )
        }
    }
}

private fun LazyListScope.appearanceSection(
    state: SettingsUiState,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    val colors = LocalAppColors.current
    item(key = "appearance-header", contentType = "header") {
        SectionHeader(title = stringResource(R.string.section_appearance))
    }
    item(key = "appearance-card", contentType = "card") {
        GroupCard {
            ThemeOptions.forEachIndexed { index, option ->
                if (index > 0) {
                    RowDivider()
                }
                val selected = state.themeMode == option.mode
                SettingsRow(
                    title = stringResource(option.titleRes),
                    subtitle = stringResource(option.subtitleRes),
                    leadingIcon = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = colors.secondaryLabel,
                            modifier = Modifier.size(AppDimens.iconSize),
                        )
                    },
                    trailing = {
                        RadioButton(
                            selected = selected,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.accent,
                            ),
                        )
                    },
                    selectableSelected = selected,
                    onClick = { onThemeModeSelected(option.mode) },
                )
            }
        }
    }
}

private fun LazyListScope.generalSection(
    state: SettingsUiState,
    onHapticsToggled: (Boolean) -> Unit,
    onOpenNotificationSettings: () -> Unit,
) {
    val colors = LocalAppColors.current
    item(key = "general-header", contentType = "header") {
        SectionHeader(title = stringResource(R.string.section_general))
    }
    item(key = "general-card", contentType = "card") {
        GroupCard {
            SettingsRow(
                title = stringResource(R.string.settings_haptics_title),
                subtitle = stringResource(R.string.settings_haptics_sub),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Vibration,
                        contentDescription = null,
                        tint = colors.secondaryLabel,
                        modifier = Modifier.size(AppDimens.iconSize),
                    )
                },
                trailing = {
                    Switch(
                        checked = state.hapticsEnabled,
                        onCheckedChange = null,
                    )
                },
                toggleableValue = state.hapticsEnabled,
                onClick = { onHapticsToggled(!state.hapticsEnabled) },
            )
            RowDivider()
            SettingsRow(
                title = stringResource(R.string.settings_notifications_title),
                subtitle = stringResource(R.string.settings_notifications_sub),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = colors.secondaryLabel,
                        modifier = Modifier.size(AppDimens.iconSize),
                    )
                },
                trailing = {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = colors.secondaryLabel,
                        modifier = Modifier.size(AppDimens.iconSize),
                    )
                },
                onClick = onOpenNotificationSettings,
            )
        }
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(SystemSettings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(SystemSettings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    colors: AppColors,
) {
    LargeTopAppBar(
        title = {
            Text(
                text = stringResource(R.string.settings_title),
                style = AppTypography.largeTitle,
            )
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = colors.groupedBackground,
            scrolledContainerColor = colors.groupedBackground,
        ),
    )
}
