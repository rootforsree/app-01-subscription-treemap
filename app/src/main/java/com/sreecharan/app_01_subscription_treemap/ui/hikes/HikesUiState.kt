package com.sreecharan.app_01_subscription_treemap.ui.hikes

import androidx.compose.runtime.Immutable
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * The price-hike inbox: only hikes the user has not acknowledged yet.
 */
@Immutable
data class HikesUiState(
    val isLoading: Boolean = true,
    val hikes: ImmutableList<PriceHike> = persistentListOf(),
)
