package com.sreecharan.app_01_subscription_treemap.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.app_01_subscription_treemap.data.repository.NotesRepository
import com.sreecharan.app_01_subscription_treemap.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    notesRepository: NotesRepository,
    @IoDispatcher ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val isRefreshingFlow = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> field = combine(
        notesRepository.observeNotes(),
        isRefreshingFlow,
    ) { notes, isRefreshing ->
        HomeUiState(
            isLoading = false,
            notes = notes.toImmutableList(),
            isRefreshing = isRefreshing,
        )
    }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true),
        )

    fun onRefresh() {
        viewModelScope.launch {
            isRefreshingFlow.value = true
            delay(REFRESH_INDICATION_MS)
            isRefreshingFlow.value = false
        }
    }

    fun onRetry() {
        onRefresh()
    }

    companion object {
        private const val REFRESH_INDICATION_MS = 800L
    }
}
