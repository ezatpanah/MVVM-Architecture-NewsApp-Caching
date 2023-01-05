package com.ezatpanah.mvvm_caching_course.utils

import com.bumptech.glide.load.engine.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = channelFlow {
    val data = query().first()

    if (shouldFetch(data)) {
        val loading = launch {
            query().collect { send(DataStatus.Loading(it)) }
        }

        try {
            delay(2000)
            saveFetchResult(fetch())
            loading.cancel()
            query().collect { send(DataStatus.Success(it)) }
        } catch (t: Throwable) {
            loading.cancel()
            query().collect { send(DataStatus.Error(t, it)) }
        }
    } else {
        query().collect { send(DataStatus.Success(it)) }
    }
}