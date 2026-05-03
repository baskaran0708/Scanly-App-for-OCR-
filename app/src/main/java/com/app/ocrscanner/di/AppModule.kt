package com.app.ocrscanner.di

import android.content.Context
import androidx.room.Room
import com.app.ocrscanner.data.local.AppDatabase
import com.app.ocrscanner.data.local.DocumentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "scanly_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDocumentDao(database: AppDatabase): DocumentDao =
        database.documentDao()
}
