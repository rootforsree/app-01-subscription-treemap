package com.sreecharan.app_01_subscription_treemap.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.sreecharan.app_01_subscription_treemap.data.datastore.SettingsProto
import com.sreecharan.app_01_subscription_treemap.data.datastore.SettingsSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private val Context.settingsDataStore by dataStore(
        fileName = "settings.pb",
        serializer = SettingsSerializer,
    )

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<SettingsProto> =
        context.settingsDataStore
}
