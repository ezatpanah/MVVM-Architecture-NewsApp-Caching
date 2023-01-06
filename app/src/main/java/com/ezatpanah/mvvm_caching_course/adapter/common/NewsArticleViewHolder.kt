package com.ezatpanah.mvvm_caching_course.adapter.common

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ezatpanah.mvvm_caching_course.R
import com.ezatpanah.mvvm_caching_course.databinding.ItemNewsArticleBinding
import com.ezatpanah.mvvm_caching_course.db.common.NewsArticle

class NewsArticleViewHolder(
    private val binding: ItemNewsArticleBinding,
    private val onItemClick: (Int) -> Unit,
    private val onBookmarkClick: (Int) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(article: NewsArticle) {
        binding.apply {
            Glide.with(itemView)
                .load(article.thumbnailUrl)
                .error(R.drawable.image_placeholder)
                .into(imageView)

            textViewTitle.text = article.title ?: ""

            imageViewBookmark.setImageResource(
                when {
                    article.isBookmarked -> R.drawable.ic_baseline_bookmark_24
                    else -> R.drawable.ic_unselected_bookmark_border_24
                }
            )
        }
    }

    init {
        binding.apply {
            root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onItemClick(pos)
                }
            }
            imageViewBookmark.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onBookmarkClick(pos)
                }
            }
        }
    }

}