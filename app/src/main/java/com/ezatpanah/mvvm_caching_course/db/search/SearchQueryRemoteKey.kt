package com.ezatpanah.mvvm_caching_course.db.search

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ezatpanah.mvvm_caching_course.utils.Constants.SEARCH_QUERY_REMOTE_KEYS

@Entity(tableName = SEARCH_QUERY_REMOTE_KEYS)
data class SearchQueryRemoteKey(
    @PrimaryKey val searchQuery: String,
    val nextPageKey: Int
)