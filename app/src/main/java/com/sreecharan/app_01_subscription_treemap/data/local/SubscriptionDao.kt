package com.sreecharan.app_01_subscription_treemap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Query("SELECT * FROM subscriptions ORDER BY name")
    fun observeAll(): Flow<List<SubscriptionEntity>>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun observeById(id: Long): Flow<SubscriptionEntity?>

    @Insert
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM subscriptions")
    suspend fun count(): Int
}
