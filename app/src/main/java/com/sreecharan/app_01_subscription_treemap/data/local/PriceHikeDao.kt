package com.sreecharan.app_01_subscription_treemap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHikeDao {

    @Query("SELECT * FROM price_hikes WHERE acknowledged = 0 ORDER BY detectedAt DESC")
    fun observeUnacknowledged(): Flow<List<PriceHikeEntity>>

    @Query("SELECT * FROM price_hikes ORDER BY detectedAt DESC")
    fun observeAll(): Flow<List<PriceHikeEntity>>

    @Insert
    suspend fun insert(hike: PriceHikeEntity): Long

    @Query("UPDATE price_hikes SET acknowledged = 1 WHERE id = :id")
    suspend fun markAcknowledged(id: Long)

    @Query("UPDATE price_hikes SET acknowledged = 1")
    suspend fun markAllAcknowledged()
}
