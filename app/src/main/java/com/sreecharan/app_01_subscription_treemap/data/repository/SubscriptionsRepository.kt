package com.sreecharan.app_01_subscription_treemap.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sreecharan.app_01_subscription_treemap.data.local.PriceHikeDao
import com.sreecharan.app_01_subscription_treemap.data.local.PriceHikeEntity
import com.sreecharan.app_01_subscription_treemap.data.local.SubscriptionDao
import com.sreecharan.app_01_subscription_treemap.data.local.SubscriptionEntity
import com.sreecharan.app_01_subscription_treemap.data.model.BillingCycle
import com.sreecharan.app_01_subscription_treemap.data.model.PriceHike
import com.sreecharan.app_01_subscription_treemap.data.model.Subscription
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionCategory
import com.sreecharan.app_01_subscription_treemap.data.model.SubscriptionInput
import com.sreecharan.app_01_subscription_treemap.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

private val Context.seedFlagsDataStore by preferencesDataStore(name = "seed_flags")
private val SEED_V1_KEY = booleanPreferencesKey("subscriptions_seeded_v1")

/** A price rise is real only when it exceeds this monthly-amount epsilon. */
internal const val HIKE_EPSILON = 0.005

sealed interface SaveResult {
    data object Saved : SaveResult

    data class HikeDetected(
        val hike: PriceHike,
    ) : SaveResult
}

/**
 * The only public entry into the subscription data layer.
 * Room is the single source of truth.
 */
interface SubscriptionsRepository {
    fun observeSubscriptions(): Flow<List<Subscription>>

    fun observeSubscription(id: Long): Flow<Subscription?>

    fun observeUnacknowledgedHikes(): Flow<List<PriceHike>>

    fun observeAllHikes(): Flow<List<PriceHike>>

    suspend fun saveSubscription(input: SubscriptionInput): SaveResult

    suspend fun deleteSubscription(id: Long)

    suspend fun acknowledgeHike(id: Long)

    suspend fun acknowledgeAllHikes()

    suspend fun seedIfNeeded()
}

/**
 * Detects a price hike when an existing subscription is edited. Returns null
 * when there is nothing to compare against (a new subscription) or when the
 * monthly amount did not rise beyond [HIKE_EPSILON].
 */
internal fun detectHike(
    old: Subscription?,
    input: SubscriptionInput,
    detectedAt: Long,
): PriceHike? {
    if (old == null) return null
    val oldMonthly = old.cycle.monthlyAmount(old.amount)
    val newMonthly = input.cycle.monthlyAmount(input.amount)
    return if (newMonthly > oldMonthly + HIKE_EPSILON) {
        PriceHike(
            id = 0,
            subscriptionId = old.id,
            subscriptionName = input.name.trim(),
            oldAmount = old.amount,
            newAmount = input.amount,
            cycle = input.cycle,
            detectedAt = detectedAt,
            acknowledged = false,
        )
    } else {
        null
    }
}

class SubscriptionsRepositoryImpl
    @Inject
    constructor(
        private val subscriptionDao: SubscriptionDao,
        private val priceHikeDao: PriceHikeDao,
        @ApplicationContext private val context: Context,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : SubscriptionsRepository {
        override fun observeSubscriptions(): Flow<List<Subscription>> =
            subscriptionDao
                .observeAll()
                .map { entities -> entities.map(SubscriptionEntity::toDomain) }
                .flowOn(ioDispatcher)

        override fun observeSubscription(id: Long): Flow<Subscription?> =
            subscriptionDao
                .observeById(id)
                .map { entity -> entity?.toDomain() }
                .flowOn(ioDispatcher)

        override fun observeUnacknowledgedHikes(): Flow<List<PriceHike>> =
            priceHikeDao
                .observeUnacknowledged()
                .map { entities -> entities.map(PriceHikeEntity::toDomain) }
                .flowOn(ioDispatcher)

        override fun observeAllHikes(): Flow<List<PriceHike>> =
            priceHikeDao
                .observeAll()
                .map { entities -> entities.map(PriceHikeEntity::toDomain) }
                .flowOn(ioDispatcher)

        override suspend fun saveSubscription(input: SubscriptionInput): SaveResult =
            withContext(ioDispatcher) {
                val now = System.currentTimeMillis()
                val id = input.id
                if (id == null) {
                    insertNewSubscription(input, now)
                    SaveResult.Saved
                } else {
                    updateExistingSubscription(input, id, now)
                }
            }

        private suspend fun insertNewSubscription(
            input: SubscriptionInput,
            now: Long,
        ) {
            subscriptionDao.insert(
                SubscriptionEntity(
                    name = input.name.trim(),
                    amount = input.amount,
                    currency = "USD",
                    cycle = input.cycle.name,
                    category = input.category.name,
                    nextBillingEpochDay = input.nextBillingEpochDay,
                    notes = input.notes.trim(),
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }

        private suspend fun updateExistingSubscription(
            input: SubscriptionInput,
            id: Long,
            now: Long,
        ): SaveResult {
            val old = subscriptionDao.observeById(id).first()?.toDomain()
            subscriptionDao.update(
                SubscriptionEntity(
                    id = id,
                    name = input.name.trim(),
                    amount = input.amount,
                    currency = old?.currency ?: "USD",
                    cycle = input.cycle.name,
                    category = input.category.name,
                    nextBillingEpochDay = input.nextBillingEpochDay,
                    notes = input.notes.trim(),
                    createdAt = old?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
            val hike = detectHike(old, input, now)
            return if (hike == null) {
                SaveResult.Saved
            } else {
                val hikeId =
                    priceHikeDao.insert(
                        PriceHikeEntity(
                            subscriptionId = hike.subscriptionId,
                            subscriptionName = hike.subscriptionName,
                            oldAmount = hike.oldAmount,
                            newAmount = hike.newAmount,
                            cycle = hike.cycle.name,
                            detectedAt = hike.detectedAt,
                            acknowledged = false,
                        ),
                    )
                SaveResult.HikeDetected(hike.copy(id = hikeId))
            }
        }

        override suspend fun deleteSubscription(id: Long) {
            withContext(ioDispatcher) {
                subscriptionDao.deleteById(id)
            }
        }

        override suspend fun acknowledgeHike(id: Long) {
            withContext(ioDispatcher) {
                priceHikeDao.markAcknowledged(id)
            }
        }

        override suspend fun acknowledgeAllHikes() {
            withContext(ioDispatcher) {
                priceHikeDao.markAllAcknowledged()
            }
        }

        override suspend fun seedIfNeeded() {
            withContext(ioDispatcher) {
                val alreadySeeded =
                    context.seedFlagsDataStore.data
                        .map { it[SEED_V1_KEY] ?: false }
                        .first()
                if (alreadySeeded) return@withContext
                for (sample in seedEntities(LocalDate.now())) {
                    subscriptionDao.insert(sample)
                }
                context.seedFlagsDataStore.updateData { prefs ->
                    prefs.toMutablePreferences().apply { set(SEED_V1_KEY, true) }
                }
            }
        }

        private fun seedEntities(today: LocalDate): List<SubscriptionEntity> {
            val now = System.currentTimeMillis()

            fun entity(
                name: String,
                amount: Double,
                category: SubscriptionCategory,
                plusDays: Int,
                notes: String,
            ) = SubscriptionEntity(
                name = name,
                amount = amount,
                currency = "USD",
                cycle = BillingCycle.MONTHLY.name,
                category = category.name,
                nextBillingEpochDay = today.plusDays(plusDays.toLong()).toEpochDay(),
                notes = notes,
                createdAt = now,
                updatedAt = now,
            )
            return listOf(
                entity(
                    name = "Netflix",
                    amount = 17.99,
                    category = SubscriptionCategory.STREAMING,
                    plusDays = 12,
                    notes = "Standard plan. Check if the ad-supported tier is enough.",
                ),
                entity(
                    name = "Spotify",
                    amount = 11.99,
                    category = SubscriptionCategory.MUSIC,
                    plusDays = 20,
                    notes = "Individual plan. Student discount expired last year.",
                ),
                entity(
                    name = "iCloud+",
                    amount = 2.99,
                    category = SubscriptionCategory.CLOUD,
                    plusDays = 5,
                    notes = "200 GB tier. Almost full, photos are the bulk of it.",
                ),
                entity(
                    name = "Notion",
                    amount = 10.00,
                    category = SubscriptionCategory.PRODUCTIVITY,
                    plusDays = 25,
                    notes = "Plus plan. Reconsider when the free tier covers the use.",
                ),
                entity(
                    name = "Disney+",
                    amount = 13.99,
                    category = SubscriptionCategory.STREAMING,
                    plusDays = 8,
                    notes = "Ad-free plan. Mostly for the kids.",
                ),
            )
        }
    }

private fun SubscriptionEntity.toDomain(): Subscription =
    Subscription(
        id = id,
        name = name,
        amount = amount,
        currency = currency,
        cycle = BillingCycle.valueOf(cycle),
        category = SubscriptionCategory.valueOf(category),
        nextBillingEpochDay = nextBillingEpochDay,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

private fun PriceHikeEntity.toDomain(): PriceHike =
    PriceHike(
        id = id,
        subscriptionId = subscriptionId,
        subscriptionName = subscriptionName,
        oldAmount = oldAmount,
        newAmount = newAmount,
        cycle = BillingCycle.valueOf(cycle),
        detectedAt = detectedAt,
        acknowledged = acknowledged,
    )
