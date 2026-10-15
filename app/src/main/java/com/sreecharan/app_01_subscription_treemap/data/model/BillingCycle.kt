package com.sreecharan.app_01_subscription_treemap.data.model

private const val MONTHS_PER_YEAR = 12.0
private const val MONTHS_PER_YEAR_INT = 12
private const val WEEKS_PER_YEAR = 52
private const val MONTHS_PER_QUARTER = 4

enum class BillingCycle(
    val periodsPerYear: Int,
    val displayName: String,
) {
    WEEKLY(WEEKS_PER_YEAR, "Weekly"),
    MONTHLY(MONTHS_PER_YEAR_INT, "Monthly"),
    QUARTERLY(MONTHS_PER_QUARTER, "Quarterly"),
    YEARLY(1, "Yearly"),
    ;

    fun monthlyAmount(amount: Double): Double = amount * periodsPerYear / MONTHS_PER_YEAR
}
