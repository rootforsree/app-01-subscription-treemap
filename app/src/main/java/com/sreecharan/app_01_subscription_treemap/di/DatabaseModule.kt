package com.sreecharan.app_01_subscription_treemap.di

import android.content.Context
import androidx.room.Room
import com.sreecharan.app_01_subscription_treemap.data.local.AppDatabase
import com.sreecharan.app_01_subscription_treemap.data.local.NoteDao
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
        Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()
}
