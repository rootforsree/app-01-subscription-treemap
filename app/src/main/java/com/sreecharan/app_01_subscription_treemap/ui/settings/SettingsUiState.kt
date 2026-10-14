package com.sreecharan.app_01_subscription_treemap.ui.settings

import androidx.compose.runtime.Immutable
import com.sreecharan.app_01_subscription_treemap.data.model.ThemeMode

@Immutable
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticsEnabled: Boolean = true,
)
