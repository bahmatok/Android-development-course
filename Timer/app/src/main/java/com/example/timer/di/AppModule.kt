package com.example.timer.di

import android.content.Context
import androidx.room.Room
import com.example.timer.data.dao.TimerSequenceDao
import com.example.timer.data.database.AppDatabase
import com.example.timer.data.repository.TimerSequenceRepositoryImpl
import com.example.timer.domain.repository.TimerSequenceRepository
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()   // на время разработки
            .build()
    }

    @Provides
    @Singleton
    fun provideTimerSequenceDao(database: AppDatabase): TimerSequenceDao {
        return database.timerSequenceDao()
    }

    @Provides
    @Singleton
    fun provideTimerSequenceRepository(
        impl: TimerSequenceRepositoryImpl
    ): TimerSequenceRepository = impl
}