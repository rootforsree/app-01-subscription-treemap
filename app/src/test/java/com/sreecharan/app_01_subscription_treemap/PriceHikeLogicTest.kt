package com.sreecharan.app_01_subscription_treemap

import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionInput
import com.sreecharan.app_01_subscription_treemap.data.repository.detectHike
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PriceHikeLogicTest {
    private val subscriptionId = 7L
    private val oldAmount = 10.0
    private val increasedAmount = 12.0
    private val decreasedAmount = 8.0
    private val epsilonIncrease = 10.004
    private val detectedAt = 123L

    private val oldSubscription =
        Subscription(
            id = subscriptionId,
            name = "Netflix",
            amount = oldAmount,
            currency = "USD",
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.STREAMING,
            nextBillingEpochDay = null,
            notes = "",
            createdAt = 0L,
            updatedAt = 0L,
        )

    private fun input(amount: Double): SubscriptionInput =
        SubscriptionInput(
            id = subscriptionId,
            name = "Netflix",
            amount = amount,
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.STREAMING,
            nextBillingEpochDay = null,
            notes = "",
        )

    @Test
    fun `a price increase returns a hike`() {
        val hike = detectHike(oldSubscription, input(increasedAmount), detectedAt)

        assertNotNull(hike)
        assertEquals(subscriptionId, hike.subscriptionId)
        assertEquals("Netflix", hike.subscriptionName)
        assertEquals(oldAmount, hike.oldAmount)
        assertEquals(increasedAmount, hike.newAmount)
        assertEquals(BillingCycle.MONTHLY, hike.cycle)
        assertEquals(detectedAt, hike.detectedAt)
        assertFalse(hike.acknowledged)
    }

    @Test
    fun `an unchanged price returns null`() {
        assertNull(detectHike(oldSubscription, input(oldAmount), detectedAt))
    }

    @Test
    fun `a price decrease returns null`() {
        assertNull(detectHike(oldSubscription, input(decreasedAmount), detectedAt))
    }

    @Test
    fun `an increase inside the epsilon returns null`() {
        assertNull(detectHike(oldSubscription, input(epsilonIncrease), detectedAt))
    }

    @Test
    fun `a new subscription with no old row returns null`() {
        assertNull(detectHike(null, input(increasedAmount), detectedAt))
    }
}
