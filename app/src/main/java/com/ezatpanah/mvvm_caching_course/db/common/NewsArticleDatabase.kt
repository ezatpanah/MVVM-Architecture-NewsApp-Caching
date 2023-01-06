package com.ezatpanah.mvvm_caching_course.db.common

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ezatpanah.mvvm_caching_course.db.search.SearchQueryRemoteKey
import com.ezatpanah.mvvm_caching_course.db.search.SearchQueryRemoteKeyDao

@Database(
    entities = [NewsArticle::class, BreakingNews::class, SearchResult::class, SearchQueryRemoteKey::class],
    version = 2
)
abstract class NewsArticleDatabase : RoomDatabase() {

    abstract fun newsArticleDao(): NewsArticleDao

    abstract fun searchQueryRemoteKeyDao(): SearchQueryRemoteKeyDao

}