package com.sreecharan.app_01_subscription_treemap.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sreecharan.app_01_subscription_treemap.R
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionInput
import com.sreecharan.app_01_subscription_treemap.data.model.monthlyAmount
import com.sreecharan.app_01_subscription_treemap.data.model.yearlyAmount
import com.sreecharan.app_01_subscription_treemap.data.repository.SaveResult
import com.sreecharan.app_01_subscription_treemap.data.repository.SubscriptionsRepository
import com.sreecharan.app_01_subscription_treemap.data.treemap.squarifiedTreemap
import com.sreecharan.app_01_subscription_treemap.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class DetailData(
    val subscription: Subscription?,
    val latestHike: PriceHike?,
)

/**
 * Home screen state owner. Combines the subscription list, the unacknowledged
 * hike count, the selected category filter, and the sheet targets into one
 * atomic [HomeUiState]; the treemap layout runs on the IO dispatcher.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val subscriptionsRepository: SubscriptionsRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val selectedCategoryFlow = MutableStateFlow<SubscriptionCategory?>(null)
    private val showAddSheetFlow = MutableStateFlow(false)
    private val editingSubscriptionFlow = MutableStateFlow<Subscription?>(null)
    private val detailSubscriptionIdFlow = MutableStateFlow<Long?>(null)
    private val refreshingFlow = MutableStateFlow(false)
    private val seedErrorFlow = MutableStateFlow<String?>(null)

    private val eventsChannel = Channel<String>(Channel.BUFFERED)

    /** One-shot UI events (snackbars). Collected once in the route. */
    val events: Flow<String> = eventsChannel.receiveAsFlow()

    private val detailFlow: Flow<DetailData> = detailSubscriptionIdFlow.flatMapLatest { id ->
        if (id == null) {
            flowOf(DetailData(subscription = null, latestHike = null))
        } else {
            combine(
                subscriptionsRepository.observeSubscription(id),
                subscriptionsRepository.observeAllHikes(),
            ) { subscription, hikes ->
                DetailData(
                    subscription = subscription,
                    latestHike = hikes
                        .filter { it.subscriptionId == id }
                        .maxByOrNull { it.detectedAt },
                )
            }
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        subscriptionsRepository.observeSubscriptions(),
        subscriptionsRepository.observeUnacknowledgedHikes(),
        selectedCategoryFlow,
        showAddSheetFlow,
        editingSubscriptionFlow,
        detailFlow,
        refreshingFlow,
        seedErrorFlow,
    ) { subscriptions, unacknowledgedHikes, selectedCategory, showAddSheet, editing, detail, isRefreshing, seedError ->
        val filtered = if (selectedCategory == null) {
            subscriptions
        } else {
            subscriptions.filter { it.category == selectedCategory }
        }
        val weights = filtered.associate { it.id to it.monthlyAmount.toFloat() }
        HomeUiState(
            isLoading = isRefreshing,
            subscriptions = filtered.toImmutableList(),
            tiles = squarifiedTreemap(weights, 1f, 1f).toImmutableList(),
            totalMonthly = subscriptions.sumOf { it.monthlyAmount },
            totalYearly = subscriptions.sumOf { it.yearlyAmount },
            categoriesPresent = subscriptions.map { it.category }.distinct().toImmutableList(),
            selectedCategory = selectedCategory,
            unacknowledgedHikeCount = unacknowledgedHikes.size,
            errorMessage = seedError,
            showAddSheet = showAddSheet,
            editingSubscription = editing,
            detailSubscriptionId = detail.subscription?.id,
            detailSubscription = detail.subscription,
            detailLatestHike = detail.latestHike,
        )
    }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true),
        )

    init {
        viewModelScope.launch {
            seed()
        }
    }

    fun onCategorySelected(category: SubscriptionCategory?) {
        selectedCategoryFlow.value = category
    }

    fun onAddClick() {
        showAddSheetFlow.value = true
    }

    fun onSubscriptionClick(id: Long) {
        detailSubscriptionIdFlow.value = id
    }

    fun onDetailDismiss() {
        detailSubscriptionIdFlow.value = null
    }

    fun onEditorDismiss() {
        showAddSheetFlow.value = false
        editingSubscriptionFlow.value = null
    }

    fun onEditSubscription(subscription: Subscription) {
        editingSubscriptionFlow.value = subscription
    }

    fun onEditFromDetail(subscription: Subscription) {
        detailSubscriptionIdFlow.value = null
        editingSubscriptionFlow.value = subscription
    }

    fun onRetry() {
        viewModelScope.launch {
            refreshingFlow.value = true
            seedErrorFlow.value = null
            seed()
            refreshingFlow.value = false
        }
    }

    fun saveSubscription(input: SubscriptionInput) {
        viewModelScope.launch {
            when (val result = subscriptionsRepository.saveSubscription(input)) {
                is SaveResult.HikeDetected -> {
                    onEditorDismiss()
                    eventsChannel.send(appContext.getString(R.string.snack_hike_recorded))
                }
                SaveResult.Saved -> onEditorDismiss()
            }
        }
    }

    fun deleteSubscription(id: Long) {
        viewModelScope.launch {
            subscriptionsRepository.deleteSubscription(id)
            onEditorDismiss()
            onDetailDismiss()
        }
    }

    private suspend fun seed() {
        try {
            subscriptionsRepository.seedIfNeeded()
        } catch (_: Exception) {
            seedErrorFlow.value = appContext.getString(R.string.home_error_message)
        }
    }
}
