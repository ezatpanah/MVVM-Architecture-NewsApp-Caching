package com.ezatpanah.mvvm_caching_course.adapter.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.ezatpanah.mvvm_caching_course.databinding.ItemNewsArticleBinding
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticle

class NewsArticleListAdapter (
    private val onItemClick: (NewsArticle) -> Unit,
    private val onBookmarkClick: (NewsArticle) -> Unit,
) : ListAdapter<NewsArticle, NewsArticleViewHolder>(NewsArticleComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsArticleViewHolder {
        val binding =
            ItemNewsArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsArticleViewHolder(
            binding,
            onItemClick = { pos ->
                val article = getItem(pos)
                if (article != null) {
                    onItemClick(article)
                }
            },
            onBookmarkClick = { pos ->
                val article = getItem(pos)
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