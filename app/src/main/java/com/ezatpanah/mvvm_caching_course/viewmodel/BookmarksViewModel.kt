package com.ezatpanah.mvvm_caching_course.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticle
import com.ezatpanah.mvvm_caching_course.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val repository: NewsRepository,
) : ViewModel() {
    val bookmarks = repository.getAllBookmarkedArticles()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun onBookmarkClick(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.updateArticle(updatedArticle)
        }
    }

    fun onDeleteAllBookmarks() {
        viewModelScope.launch {
            repository.resetAllBookmarks()
        }
    }
}