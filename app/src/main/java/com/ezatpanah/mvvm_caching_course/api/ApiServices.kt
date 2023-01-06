package com.ezatpanah.mvvm_caching_course.api

import com.ezatpanah.mvvm_caching_course.response.NewsResponse
import com.ezatpanah.mvvm_caching_course.utils.Constants.API_KEY
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiServices{

    //https://newsapi.org/v2/top-headlines?country=us&apiKey=****

    @Headers("X-Api-Key: $API_KEY")
    @GET("top-headlines?country=us&pageSize=100")
    suspend fun getBreakingNews(): NewsResponse

    @Headers("X-Api-Key: $API_KEY")
    @GET("everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): NewsResponse

}