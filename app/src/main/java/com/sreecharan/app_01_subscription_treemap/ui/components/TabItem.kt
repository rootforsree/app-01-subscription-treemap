package com.sreecharan.app_01_subscription_treemap.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

@Immutable
data class TabItem(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)
