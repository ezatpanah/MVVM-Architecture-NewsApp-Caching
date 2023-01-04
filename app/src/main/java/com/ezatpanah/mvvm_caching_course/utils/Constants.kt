package com.ezatpanah.mvvm_caching_course.utils

import com.ezatpanah.mvvm_caching_course.BuildConfig

class Constants {

    object Constants {
        const val NETWORK_TIMEOUT = 60L

        const val BASE_URL = "https://newsapi.org/v2/"
        const val API_KEY = BuildConfig.NEWS_API_ACCESS_KEY


        const val NEWS_TABLE = "news_table"
        const val NEWS_DATABASE = "news_database"

    }

}