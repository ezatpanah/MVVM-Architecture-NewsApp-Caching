package com.ezatpanah.mvvm_caching_course.db.common

import androidx.paging.PagingSource
import androidx.room.*
import com.ezatpanah.mvvm_caching_course.utils.Constants.BREAKING_NEWS_TABLE
import com.ezatpanah.mvvm_caching_course.utils.Constants.NEWS_ARTICLES_TABLE
import com.ezatpanah.mvvm_caching_course.utils.Constants.SEARCH_RESULTS_TABLE
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

    @Update
    suspend fun updateArticle(article: NewsArticle)

    @Query("UPDATE $NEWS_ARTICLES_TABLE SET isBookmarked = 0")
    suspend fun resetAllBookmarks()

    @Query("SELECT * FROM $NEWS_ARTICLES_TABLE WHERE isBookmarked = 1")
    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(searchResults: List<SearchResult>)

    @Query("DELETE FROM $SEARCH_RESULTS_TABLE WHERE searchQuery = :query")
    suspend fun deleteSearchResultsForQuery(query: String)

    @Query("SELECT MAX(queryPosition) FROM $SEARCH_RESULTS_TABLE WHERE searchQuery = :searchQuery")
    suspend fun getLastQueryPosition(searchQuery: String): Int?

    @Query("SELECT * FROM $SEARCH_RESULTS_TABLE INNER JOIN $NEWS_ARTICLES_TABLE ON articleUrl = url WHERE searchQuery = :query ORDER BY queryPosition")
    fun getSearchResultArticlesPaged(query: String): PagingSource<Int, NewsArticle>

}