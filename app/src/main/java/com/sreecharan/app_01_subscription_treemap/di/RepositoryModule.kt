package com.sreecharan.app_01_subscription_treemap.di

import com.sreecharan.app_01_subscription_treemap.data.datastore.SettingsRepository
import com.sreecharan.app_01_subscription_treemap.data.datastore.SettingsRepositoryImpl
import com.sreecharan.app_01_subscription_treemap.data.repository.NotesRepository
import com.sreecharan.app_01_subscription_treemap.data.repository.NotesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindNotesRepository(impl: NotesRepositoryImpl): NotesRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
