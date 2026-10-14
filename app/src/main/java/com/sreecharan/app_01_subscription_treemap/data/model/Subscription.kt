package com.sreecharan.app_01_subscription_treemap.data.model

private const val MONTHS_PER_YEAR = 12.0

/**
 * A recurring subscription. The domain representation of
 * [com.sreecharan.app_01_subscription_treemap.data.local.SubscriptionEntity].
 *
 * Amounts are in [currency] units. [nextBillingEpochDay] follows [java.time.LocalDate.getEpochDay];
 * null means the next billing date is unknown.
 */
data class Subscription(
    val id: Long,
    val name: String,
    val amount: Double,
    val currency: String = "USD",
    val cycle: BillingCycle,
    val category: SubscriptionCategory,
    val nextBillingEpochDay: Long?,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
)

val Subscription.monthlyAmount: Double
    get() = cycle.monthlyAmount(amount)

val Subscription.yearlyAmount: Double
    get() = monthlyAmount * MONTHS_PER_YEAR
