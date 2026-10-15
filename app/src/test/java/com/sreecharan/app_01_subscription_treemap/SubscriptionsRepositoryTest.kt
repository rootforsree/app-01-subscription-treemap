package com.sreecharan.app_01_subscription_treemap

import androidx.room.Room
import com.sreecharan.app_01_subscription_treemap.data.local.AppDatabase
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionInput
import com.sreecharan.app_01_subscription_treemap.data.repository.SaveResult
import com.sreecharan.app_01_subscription_treemap.data.repository.SubscriptionsRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Room-backed repository test on Robolectric (JUnit4). Executes on CI only;
 * never run Gradle locally in this environment.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SubscriptionsRepositoryTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: SubscriptionsRepositoryImpl

    private val baseAmount = 10.0
    private val increasedAmount = 12.0
    private val decreasedAmount = 8.0
    private val expectedSeedCount = 5

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        database =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository =
            SubscriptionsRepositoryImpl(
                subscriptionDao = database.subscriptionDao(),
                priceHikeDao = database.priceHikeDao(),
                context = context,
                ioDispatcher = dispatcher,
            )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun input(
        name: String = "Netflix",
        amount: Double = baseAmount,
    ): SubscriptionInput =
        SubscriptionInput(
            name = name,
            amount = amount,
            cycle = BillingCycle.MONTHLY,
            category = SubscriptionCategory.STREAMING,
            nextBillingEpochDay = null,
            notes = "A sample subscription.",
        )

    @Test
    fun `saving a new subscription emits it through observeSubscriptions`() =
        runTest(dispatcher) {
            val result = repository.saveSubscription(input())

            assertTrue(result is SaveResult.Saved)
            val subscriptions = repository.observeSubscriptions().first()
            assertEquals(1, subscriptions.size)
            assertEquals("Netflix", subscriptions.first().name)
            assertEquals(baseAmount, subscriptions.first().amount)
        }

    @Test
    fun `editing with a price increase records a hike`() =
        runTest(dispatcher) {
            repository.saveSubscription(input())
            val id =
                repository
                    .observeSubscriptions()
                    .first()
                    .first()
                    .id

            val result = repository.saveSubscription(input(amount = increasedAmount).copy(id = id))

            assertTrue(result is SaveResult.HikeDetected)
            val hike = (result as SaveResult.HikeDetected).hike
            assertEquals(baseAmount, hike.oldAmount)
            assertEquals(increasedAmount, hike.newAmount)

            val unacknowledged = repository.observeUnacknowledgedHikes().first()
            assertEquals(1, unacknowledged.size)
            assertEquals("Netflix", unacknowledged.first().subscriptionName)
            assertEquals(baseAmount, unacknowledged.first().oldAmount)
            assertEquals(increasedAmount, unacknowledged.first().newAmount)
        }

    @Test
    fun `editing with the same price saves without a hike`() =
        runTest(dispatcher) {
            repository.saveSubscription(input())
            val id =
                repository
                    .observeSubscriptions()
                    .first()
                    .first()
                    .id

            val result = repository.saveSubscription(input(amount = baseAmount).copy(id = id))

            assertTrue(result is SaveResult.Saved)
            assertTrue(repository.observeUnacknowledgedHikes().first().isEmpty())
        }

    @Test
    fun `editing with a price decrease saves without a hike`() =
        runTest(dispatcher) {
            repository.saveSubscription(input())
            val id =
                repository
                    .observeSubscriptions()
                    .first()
                    .first()
                    .id

            val result = repository.saveSubscription(input(amount = decreasedAmount).copy(id = id))

            assertTrue(result is SaveResult.Saved)
            assertTrue(repository.observeUnacknowledgedHikes().first().isEmpty())
        }

    @Test
    fun `deleteSubscription removes the subscription`() =
        runTest(dispatcher) {
            repository.saveSubscription(input(name = "Netflix"))
            repository.saveSubscription(input(name = "Spotify"))
            val id =
                repository
                    .observeSubscriptions()
                    .first()
                    .first { it.name == "Netflix" }
                    .id

            repository.deleteSubscription(id)

            val remaining = repository.observeSubscriptions().first()
            assertEquals(1, remaining.size)
            assertEquals("Spotify", remaining.first().name)
        }

    @Test
    fun `acknowledgeHike clears it from the unacknowledged flow`() =
        runTest(dispatcher) {
            repository.saveSubscription(input())
            val id =
                repository
                    .observeSubscriptions()
                    .first()
                    .first()
                    .id
            val saved = repository.saveSubscription(input(amount = increasedAmount).copy(id = id))
            val hikeId = (saved as SaveResult.HikeDetected).hike.id

            repository.acknowledgeHike(hikeId)

            assertTrue(repository.observeUnacknowledgedHikes().first().isEmpty())
            assertEquals(1, repository.observeAllHikes().first().size)
        }

    @Test
    fun `seedIfNeeded inserts five samples exactly once`() =
        runTest(dispatcher) {
            repository.seedIfNeeded()
            repository.seedIfNeeded()

            val subscriptions = repository.observeSubscriptions().first()
            assertEquals(expectedSeedCount, subscriptions.size)
            assertEquals(
                setOf("Netflix", "Spotify", "iCloud+", "Notion", "Disney+"),
                subscriptions.map { it.name }.toSet(),
            )
        }
}
