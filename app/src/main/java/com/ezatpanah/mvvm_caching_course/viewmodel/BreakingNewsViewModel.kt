package com.ezatpanah.mvvm_caching_course.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticle
import com.ezatpanah.mvvm_caching_course.repository.NewsRepository
import com.ezatpanah.mvvm_caching_course.utils.DataStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class BreakingNewsViewModel @Inject constructor(
    private val repository: NewsRepository,
) : ViewModel() {

    private val refreshTriggerChannel = Channel<Refresh>()
    private val refreshTrigger = refreshTriggerChannel.receiveAsFlow()
    private val eventChannel = Channel<Event>()
    val events = eventChannel.receiveAsFlow()
    var pendingScrollToTopAfterRefresh = false

    val breakingNews = refreshTrigger
        .flatMapLatest { refresh ->
            repository.getBreakingNews(
                refresh == Refresh.FORCE,
                onFetchSuccess = {
                    pendingScrollToTopAfterRefresh = true
                },
                onFetchFailed = { t ->
                    viewModelScope.launch {
                        eventChannel.send(Event.ShowErrorMessage(t))
                    }
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            repository.deleteNonBookmarkedArticlesOlderThan(
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            )
        }
    }

    fun onStart() {
        if (breakingNews.value !is DataStatus.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.NORMAL)
            }
        }
    }

    fun onManualRefresh() {
        if (breakingNews.value !is DataStatus.Loading) {
            viewModelScope.launch {
                refreshTriggerChannel.send(Refresh.FORCE)
            }
        }
    }

    fun onBookmarkClick(article: NewsArticle) {
        val currentlyBookmarked = article.isBookmarked
        val updatedArticle = article.copy(isBookmarked = !currentlyBookmarked)
        viewModelScope.launch {
            repository.updateArticle(updatedArticle)
        }
    }

    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }

    enum class Refresh {
        FORCE, NORMAL
    }
}