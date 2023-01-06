package com.ezatpanah.mvvm_caching_course.db.common

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ezatpanah.mvvm_caching_course.utils.Constants.BREAKING_NEWS_TABLE
import com.ezatpanah.mvvm_caching_course.utils.Constants.NEWS_ARTICLES_TABLE
import com.ezatpanah.mvvm_caching_course.utils.Constants.SEARCH_RESULTS_TABLE

@Entity(tableName = NEWS_ARTICLES_TABLE)
data class NewsArticle(
    val title: String?,
    @PrimaryKey val url: String,
    val thumbnailUrl: String?,
    val isBookmarked: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = BREAKING_NEWS_TABLE)
data class BreakingNews(
    val articleUrl: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

@Entity(tableName = SEARCH_RESULTS_TABLE, primaryKeys = ["searchQuery", "articleUrl"])
data class SearchResult(
    val searchQuery: String,
    val articleUrl: String,
    val queryPosition: Int
)