package com.ezatpanah.mvvm_caching_course.db.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ezatpanah.mvvm_caching_course.utils.Constants.SEARCH_QUERY_REMOTE_KEYS

@Dao
interface SearchQueryRemoteKeyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteKey(remoteKey: SearchQueryRemoteKey)

    @Query("SELECT * FROM $SEARCH_QUERY_REMOTE_KEYS WHERE searchQuery = :searchQuery")
    suspend fun getRemoteKey(searchQuery: String): SearchQueryRemoteKey

}