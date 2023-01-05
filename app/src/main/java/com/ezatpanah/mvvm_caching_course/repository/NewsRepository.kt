package com.ezatpanah.mvvm_caching_course.repository

import androidx.room.withTransaction
import com.bumptech.glide.load.engine.Resource
import com.ezatpanah.mvvm_caching_course.api.ApiServices
import com.ezatpanah.mvvm_caching_course.db.BreakingNews
import com.ezatpanah.mvvm_caching_course.db.NewsArticle
import com.ezatpanah.mvvm_caching_course.db.NewsArticleDatabase
import com.ezatpanah.mvvm_caching_course.utils.DataStatus
import com.ezatpanah.mvvm_caching_course.utils.networkBoundResource
import kotlinx.coroutines.flow.Flow
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
                val breakingNewsArticles =
                    serverBreakingNewsArticles.map { serverBreakingNewsArticle ->
                        NewsArticle(
                            title = serverBreakingNewsArticle.title,
                            url = serverBreakingNewsArticle.url,
                            thumbnailUrl = serverBreakingNewsArticle.urlToImage,
                            isBookmarked = false
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

    suspend fun deleteNonBookmarkedArticlesOlderThan(timestampInMillis: Long) {
        newsArticleDao.deleteNonBookmarkedArticlesOlderThan(timestampInMillis)
    }


}