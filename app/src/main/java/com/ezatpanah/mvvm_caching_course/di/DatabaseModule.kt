package com.ezatpanah.mvvm_caching_course.di

import android.content.Context
import androidx.room.Room
import com.ezatpanah.mvvm_caching_course.db.NewsArticleDatabase
import com.ezatpanah.mvvm_caching_course.utils.Constants.Constants.NEWS_DATABASE
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
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, NewsArticleDatabase::class.java, NEWS_DATABASE
    )
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()

}