package com.ezatpanah.mvvm_caching_course.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ezatpanah.mvvm_caching_course.utils.Constants.Constants.BREAKING_NEWS_TABLE
import com.ezatpanah.mvvm_caching_course.utils.Constants.Constants.NEWS_ARTICLES_TABLE

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