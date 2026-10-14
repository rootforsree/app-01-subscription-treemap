package com.sreecharan.app_01_subscription_treemap.di

import android.content.Context
import androidx.room.Room
import com.sreecharan.app_01_subscription_treemap.data.local.AppDatabase
import com.sreecharan.app_01_subscription_treemap.data.local.PriceHikeDao
import com.sreecharan.app_01_subscription_treemap.data.local.SubscriptionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "subscriptions.db").build()

    @Provides
    fun provideSubscriptionDao(database: AppDatabase): SubscriptionDao = database.subscriptionDao()

    @Provides
    fun providePriceHikeDao(database: AppDatabase): PriceHikeDao = database.priceHikeDao()
}
