package com.hodnex.todoapp.di

import android.app.Application
import androidx.room.Room
import com.hodnex.todoapp.data.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: TaskDatabase.Callback
    ) = Room.databaseBuilder(app, TaskDatabase::class.java, "task_database")
            .fallbackToDestructiveMigration()
            .addCallback(callback)
            .build()

    @Provides
    @Singleton
    fun provideTaskDao(tb: TaskDatabase) = tb.taskDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope