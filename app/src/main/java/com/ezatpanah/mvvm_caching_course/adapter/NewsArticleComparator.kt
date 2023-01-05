package com.ezatpanah.mvvm_caching_course.adapter

import androidx.recyclerview.widget.DiffUtil
import com.ezatpanah.mvvm_caching_course.db.NewsArticle

class NewsArticleComparator : DiffUtil.ItemCallback<NewsArticle>() {

    override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle) =
        oldItem.url == newItem.url

    override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle) =
        oldItem == newItem
}