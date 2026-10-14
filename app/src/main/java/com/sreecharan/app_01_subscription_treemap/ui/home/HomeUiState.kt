package com.sreecharan.app_01_subscription_treemap.ui.home

import androidx.compose.runtime.Immutable
import com.sreecharan.app_01_subscription_treemap.data.model.Note
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val notes: ImmutableList<Note> = persistentListOf(),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
)
