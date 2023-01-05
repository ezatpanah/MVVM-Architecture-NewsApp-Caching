package com.ezatpanah.mvvm_caching_course.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ezatpanah.mvvm_caching_course.utils.Constants.Constants.BREAKING_NEWS_TABLE
import com.ezatpanah.mvvm_caching_course.utils.Constants.Constants.NEWS_ARTICLES_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsArticleDao {

    @Query("SELECT * FROM $BREAKING_NEWS_TABLE INNER JOIN $NEWS_ARTICLES_TABLE ON articleUrl = url")
    fun getAllBreakingNewsArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticle>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreakingNews(breakingNews: List<BreakingNews>)

    @Query("DELETE FROM $BREAKING_NEWS_TABLE")
    suspend fun deleteAllBreakingNews()

    @Query("DELETE FROM $NEWS_ARTICLES_TABLE WHERE updatedAt < :timestampInMillis AND isBookmarked = 0")
    suspend fun deleteNonBookmarkedArticlesOlderThan(timestampInMillis: Long)
}