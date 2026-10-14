package com.sreecharan.app_01_subscription_treemap.data.model

/**
 * Unvalidated user input for a subscription. `id == null` means a new subscription;
 * otherwise the id of the existing subscription to update.
 */
data class SubscriptionInput(
    val id: Long? = null,
    val name: String,
    val amount: Double,
    val cycle: BillingCycle,
    val category: SubscriptionCategory,
    val nextBillingEpochDay: Long?,
    val notes: String,
)
