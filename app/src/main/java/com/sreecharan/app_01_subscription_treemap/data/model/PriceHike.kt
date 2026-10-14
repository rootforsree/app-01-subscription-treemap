package com.sreecharan.app_01_subscription_treemap.data.model

private const val PERCENT_FACTOR = 100.0
private const val ZERO_AMOUNT = 0.0

/**
 * A detected price increase on an existing subscription. [oldAmount] and [newAmount]
 * are the per-period amounts in the subscription's billing cycle.
 */
data class PriceHike(
    val id: Long,
    val subscriptionId: Long,
    val subscriptionName: String,
    val oldAmount: Double,
    val newAmount: Double,
    val cycle: BillingCycle,
    val detectedAt: Long,
    val acknowledged: Boolean,
)

val PriceHike.oldMonthly: Double
    get() = cycle.monthlyAmount(oldAmount)

val PriceHike.newMonthly: Double
    get() = cycle.monthlyAmount(newAmount)

val PriceHike.percentIncrease: Double
    get() {
        if (oldMonthly == ZERO_AMOUNT) return ZERO_AMOUNT
        return (newMonthly - oldMonthly) / oldMonthly * PERCENT_FACTOR
    }
