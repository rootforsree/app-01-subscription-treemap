package com.sreecharan.app_01_subscription_treemap

import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BillingCycleTest {
    private val tolerance = 1e-9
    private val tenDollars = 10.0
    private val twelveFifty = 12.5
    private val weeklyExpected = tenDollars * 52.0 / 12.0
    private val quarterlyExpected = tenDollars / 3.0
    private val yearlyAmount = 120.0
    private val zero = 0.0

    @Test
    fun `weekly 10 dollars converts to 43 point 333 monthly`() {
        assertEquals(weeklyExpected, BillingCycle.WEEKLY.monthlyAmount(tenDollars), tolerance)
    }

    @Test
    fun `monthly cycle keeps the amount unchanged`() {
        assertEquals(twelveFifty, BillingCycle.MONTHLY.monthlyAmount(twelveFifty), tolerance)
    }

    @Test
    fun `quarterly amount divides by three`() {
        assertEquals(quarterlyExpected, BillingCycle.QUARTERLY.monthlyAmount(tenDollars), tolerance)
    }

    @Test
    fun `yearly amount divides by 12`() {
        assertEquals(tenDollars, BillingCycle.YEARLY.monthlyAmount(yearlyAmount), tolerance)
    }

    @Test
    fun `zero amount stays zero on every cycle`() {
        for (cycle in BillingCycle.entries) {
            assertEquals(zero, cycle.monthlyAmount(zero), tolerance)
        }
    }
}
