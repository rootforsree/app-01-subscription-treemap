package com.sreecharan.app_01_subscription_treemap.ui.hikes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.app_01_subscription_treemap.data.repository.SubscriptionsRepository
import com.sreecharan.app_01_subscription_treemap.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns the price-hike inbox. Acknowledging a hike removes it from the list;
 * the tab badge counts whatever remains here.
 */
@HiltViewModel
class HikesViewModel @Inject constructor(
    private val subscriptionsRepository: SubscriptionsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val uiState: StateFlow<HikesUiState> field = subscriptionsRepository.observeAllHikes()
        .map { hikes ->
            HikesUiState(
                isLoading = false,
                hikes = hikes.filter { !it.acknowledged }.toImmutableList(),
            )
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HikesUiState(isLoading = true),
        )

    fun onAcknowledge(id: Long) {
        viewModelScope.launch {
            subscriptionsRepository.acknowledgeHike(id)
        }
    }

    fun onAcknowledgeAll() {
        viewModelScope.launch {
            subscriptionsRepository.acknowledgeAllHikes()
        }
    }
}
