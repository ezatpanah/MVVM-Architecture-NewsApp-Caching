package com.ezatpanah.mvvm_caching_course.utils

import com.ezatpanah.mvvm_caching_course.BuildConfig

object Constants {

    const val NETWORK_TIMEOUT = 60L

    const val BASE_URL = "https://newsapi.org/v2/"
    const val API_KEY = BuildConfig.NEWS_API_ACCESS_KEY

    const val NEWS_DATABASE = "news_database"
    const val NEWS_ARTICLES_TABLE = "news_articles_table"
    const val BREAKING_NEWS_TABLE = "breaking_news_table"
    const val SEARCH_RESULTS_TABLE = "search_results_table"
    const val SEARCH_QUERY_REMOTE_KEYS = "search_query_remote_keys"

    const val NEWS_STARTING_PAGE_INDEX = 1

}

