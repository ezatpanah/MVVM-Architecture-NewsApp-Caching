package com.ezatpanah.mvvm_caching_course.adapter.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ezatpanah.mvvm_caching_course.R
import com.ezatpanah.mvvm_caching_course.databinding.LoadStateFooterBinding


class NewsArticleLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<NewsArticleLoadStateAdapter.LoadStateViewHolder>() {

    private lateinit var binding: LoadStateFooterBinding

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        binding = LoadStateFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadStateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    inner class LoadStateViewHolder(private val binding: LoadStateFooterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.buttonRetry.setOnClickListener {
                retry()
            }
        }

        fun bind(loadState: LoadState) {
            binding.apply {
                progressBar.isVisible = loadState is LoadState.Loading
                buttonRetry.isVisible = loadState is LoadState.Error
                textViewError.isVisible = loadState is LoadState.Error

                if (loadState is LoadState.Error) {
                    textViewError.text = loadState.error.localizedMessage
                        ?: binding.root.context.getString(R.string.unknown_error_occurred)
                }
            }
        }
    }
}