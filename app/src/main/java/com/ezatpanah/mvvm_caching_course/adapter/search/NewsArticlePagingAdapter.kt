package com.ezatpanah.mvvm_caching_course.adapter.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import com.ezatpanah.mvvm_caching_course.adapter.common.NewsArticleComparator
import com.ezatpanah.mvvm_caching_course.adapter.common.NewsArticleViewHolder
import com.ezatpanah.mvvm_caching_course.databinding.ItemNewsArticleBinding
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticle

class NewsArticlePagingAdapter(
    private val onItemClick: (NewsArticle) -> Unit,
    private val onBookmarkClick: (NewsArticle) -> Unit
) : PagingDataAdapter<NewsArticle, NewsArticleViewHolder>(NewsArticleComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsArticleViewHolder {
        val binding =
            ItemNewsArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsArticleViewHolder(binding,
            onItemClick = { position ->
                val article = getItem(position)
                if (article != null) {
                    onItemClick(article)
                }
            },
            onBookmarkClick = { position ->
                val article = getItem(position)
                if (article != null) {
                    onBookmarkClick(article)
                }
            }
        )
    }

    override fun onBindViewHolder(holder: NewsArticleViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }
}