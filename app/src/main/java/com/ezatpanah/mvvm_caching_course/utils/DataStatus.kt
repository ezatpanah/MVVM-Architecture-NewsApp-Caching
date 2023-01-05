package com.ezatpanah.mvvm_caching_course.utils

import com.bumptech.glide.load.engine.Resource

sealed class DataStatus<T>(
    val data: T? = null,
    val error: Throwable? = null,
) {
    class Success<T>(data: T) : DataStatus<T>(data)
    class Loading<T>(data: T? = null) : DataStatus<T>(data)
    class Error<T>(throwable: Throwable, data: T? = null) : DataStatus<T>(data, throwable)
}