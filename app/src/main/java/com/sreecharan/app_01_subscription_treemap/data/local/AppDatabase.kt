package com.sreecharan.app_01_subscription_treemap.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SubscriptionEntity::class, PriceHikeEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    abstract fun priceHikeDao(): PriceHikeDao
}
