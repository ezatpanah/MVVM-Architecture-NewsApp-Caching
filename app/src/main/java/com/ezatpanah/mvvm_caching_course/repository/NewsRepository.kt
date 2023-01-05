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
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val newsApi: ApiServices,
    private val newsArticleDb: NewsArticleDatabase,
) {

    private val newsArticleDao = newsArticleDb.newsArticleDao()

    fun getBreakingNews(): Flow<DataStatus<List<NewsArticle>>> =
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
            }
        )

}