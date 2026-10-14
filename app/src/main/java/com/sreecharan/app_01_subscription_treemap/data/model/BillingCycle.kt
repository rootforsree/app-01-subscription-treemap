package com.sreecharan.app_01_subscription_treemap.data.model

private const val MONTHS_PER_YEAR = 12.0

enum class BillingCycle(val periodsPerYear: Int, val displayName: String) {
    WEEKLY(52, "Weekly"),
    MONTHLY(12, "Monthly"),
    QUARTERLY(4, "Quarterly"),
    YEARLY(1, "Yearly"),
    ;

    fun monthlyAmount(amount: Double): Double = amount * periodsPerYear / MONTHS_PER_YEAR
}
