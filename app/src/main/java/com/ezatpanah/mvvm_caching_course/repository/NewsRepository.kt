package com.ezatpanah.mvvm_caching_course.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.ezatpanah.mvvm_caching_course.api.ApiServices
import com.ezatpanah.mvvm_caching_course.db.common.BreakingNews
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticle
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticleDatabase
import com.ezatpanah.mvvm_caching_course.db.search.SearchNewsRemoteMediator
import com.ezatpanah.mvvm_caching_course.utils.DataStatus
import com.ezatpanah.mvvm_caching_course.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import java.util.concurrent.TimeUnit


class NewsRepository @Inject constructor(
    private val newsApi: ApiServices,
    private val newsArticleDb: NewsArticleDatabase,
) {

    private val newsArticleDao = newsArticleDb.newsArticleDao()

    fun getBreakingNews(
        forceRefresh: Boolean,
        onFetchSuccess: () -> Unit,
        onFetchFailed: (Throwable) -> Unit,
    ): Flow<DataStatus<List<NewsArticle>>> =
        networkBoundResource(
            query = {
                newsArticleDao.getAllBreakingNewsArticles()
            },
            fetch = {
                val response = newsApi.getBreakingNews()
                response.articles
            },
            saveFetchResult = { serverBreakingNewsArticles ->
                val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()
                val breakingNewsArticles =
                    serverBreakingNewsArticles.map { serverBreakingNewsArticle ->
                        val isBookmarked = bookmarkedArticles.any {
                            it.url == serverBreakingNewsArticle.url
                        }
                        NewsArticle(
                            title = serverBreakingNewsArticle.title,
                            url = serverBreakingNewsArticle.url,
                            thumbnailUrl = serverBreakingNewsArticle.urlToImage,
                            isBookmarked = isBookmarked
                        )
                    }

                val breakingNews = breakingNewsArticles.map { article ->
                    BreakingNews(article.url)
                }

                newsArticleDb.withTransaction {
                    newsArticleDao.deleteAllBreakingNews()
                    newsArticleDao.insertArticles(breakingNewsArticles)
                    newsArticleDao.insertBreakingNews(breakingNews)
                }
            },
            shouldFetch ={ cachedArticles ->
                if (forceRefresh) {
                    true
                } else {
                    val sortedArticles = cachedArticles.sortedBy { article ->
                        article.updatedAt
                    }
                    val oldestTimestamp = sortedArticles.firstOrNull()?.updatedAt
                    val needsRefresh = oldestTimestamp == null ||
                            oldestTimestamp < System.currentTimeMillis() -
                            TimeUnit.MINUTES.toMillis(5)
                    needsRefresh
                }
            },
            onFetchSuccess = onFetchSuccess,
            onFetchFailed = { t ->
                if (t !is HttpException && t !is IOException) {
                    throw t
                }
                onFetchFailed(t)
            }
        )

    fun getAllBookmarkedArticles(): Flow<List<NewsArticle>> =
        newsArticleDao.getAllBookmarkedArticles()

    suspend fun updateArticle(article: NewsArticle) {
        newsArticleDao.updateArticle(article)
    }

    suspend fun resetAllBookmarks() {
        newsArticleDao.resetAllBookmarks()
    }

    suspend fun deleteNonBookmarkedArticlesOlderThan(timestampInMillis: Long) {
        newsArticleDao.deleteNonBookmarkedArticlesOlderThan(timestampInMillis)
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getSearchResultsPaged(query: String): Flow<PagingData<NewsArticle>> =
        Pager(
            config = PagingConfig(pageSize = 20, maxSize = 200),
            remoteMediator = SearchNewsRemoteMediator(query, newsApi, newsArticleDb),
            pagingSourceFactory = { newsArticleDao.getSearchResultArticlesPaged(query) }
        ).flow

}