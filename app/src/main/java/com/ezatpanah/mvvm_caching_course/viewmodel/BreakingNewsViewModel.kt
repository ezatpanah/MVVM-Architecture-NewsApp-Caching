package com.ezatpanah.mvvm_caching_course.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezatpanah.mvvm_caching_course.db.NewsArticle
import com.ezatpanah.mvvm_caching_course.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreakingNewsViewModel @Inject constructor(
    private val repository: NewsRepository,
) : ViewModel() {

    val breakingNews = repository.getBreakingNews()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

}