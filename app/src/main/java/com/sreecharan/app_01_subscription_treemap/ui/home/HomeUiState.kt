package com.sreecharan.app_01_subscription_treemap.ui.home

import androidx.compose.runtime.Immutable
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.treemap.TreemapTile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * One atomic snapshot of the home screen. [subscriptions] and [tiles] are
 * already filtered by [selectedCategory]; [totalMonthly]/[totalYearly] always
 * cover every subscription the user owns.
 */
@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val subscriptions: ImmutableList<Subscription> = persistentListOf(),
    val tiles: ImmutableList<TreemapTile> = persistentListOf(),
    val totalMonthly: Double = 0.0,
    val totalYearly: Double = 0.0,
    val categoriesPresent: ImmutableList<SubscriptionCategory> = persistentListOf(),
    val selectedCategory: SubscriptionCategory? = null,
    val unacknowledgedHikeCount: Int = 0,
    val errorMessage: String? = null,
    val showAddSheet: Boolean = false,
    val editingSubscription: Subscription? = null,
    val detailSubscriptionId: Long? = null,
    val detailSubscription: Subscription? = null,
    val detailLatestHike: PriceHike? = null,
)
