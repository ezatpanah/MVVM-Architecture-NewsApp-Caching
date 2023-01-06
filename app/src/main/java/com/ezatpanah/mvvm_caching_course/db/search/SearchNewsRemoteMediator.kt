package com.ezatpanah.mvvm_caching_course.db.search

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ezatpanah.mvvm_caching_course.api.ApiServices
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticle
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticleDatabase
import com.ezatpanah.mvvm_caching_course.db.common.SearchResult
import com.ezatpanah.mvvm_caching_course.utils.Constants.NEWS_STARTING_PAGE_INDEX
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.io.IOException


@OptIn(ExperimentalPagingApi::class)
class SearchNewsRemoteMediator(
    private val searchQuery: String,
    private val newsApi: ApiServices,
    private val newsArticleDb: NewsArticleDatabase
) : RemoteMediator<Int, NewsArticle>() {

    private val newsArticleDao = newsArticleDb.newsArticleDao()
    private val searchQueryRemoteKeyDao = newsArticleDb.searchQueryRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NewsArticle>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> NEWS_STARTING_PAGE_INDEX
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> searchQueryRemoteKeyDao.getRemoteKey(searchQuery).nextPageKey
        }

        try {
            val response = newsApi.searchNews(searchQuery, page, state.config.pageSize)
            delay(3000)
            val serverSearchResults = response.articles

            val bookmarkedArticles = newsArticleDao.getAllBookmarkedArticles().first()

            val searchResultArticles = serverSearchResults.map { serverSearchResultArticle ->
                val isBookmarked = bookmarkedArticles.any { bookmarkedArticle ->
                    bookmarkedArticle.url == serverSearchResultArticle.url
                }

                NewsArticle(
                    title = serverSearchResultArticle.title,
                    url = serverSearchResultArticle.url,
                    thumbnailUrl = serverSearchResultArticle.urlToImage,
                    isBookmarked = isBookmarked
                )
            }

            newsArticleDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    newsArticleDao.deleteSearchResultsForQuery(searchQuery)
                }

                val lastQueryPosition = newsArticleDao.getLastQueryPosition(searchQuery) ?: 0
                var queryPosition = lastQueryPosition + 1

                val searchResults = searchResultArticles.map { article ->
                    SearchResult(searchQuery, article.url, queryPosition++)
                }

                val nextPageKey = page + 1

                newsArticleDao.insertArticles(searchResultArticles)
                newsArticleDao.insertSearchResults(searchResults)
                searchQueryRemoteKeyDao.insertRemoteKey(
                    SearchQueryRemoteKey(searchQuery, nextPageKey)
                )
            }
            return MediatorResult.Success(endOfPaginationReached = serverSearchResults.isEmpty())
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }
}