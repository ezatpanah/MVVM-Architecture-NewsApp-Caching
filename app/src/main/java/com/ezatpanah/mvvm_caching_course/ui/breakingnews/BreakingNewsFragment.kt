package com.ezatpanah.mvvm_caching_course.ui.breakingnews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezatpanah.mvvm_caching_course.R
import com.ezatpanah.mvvm_caching_course.adapter.NewsArticleListAdapter
import com.ezatpanah.mvvm_caching_course.databinding.FragmentBreakingNewsBinding
import com.ezatpanah.mvvm_caching_course.utils.DataStatus
import com.ezatpanah.mvvm_caching_course.viewmodel.BreakingNewsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@AndroidEntryPoint
class BreakingNewsFragment : Fragment() {

    private val viewModel: BreakingNewsViewModel by viewModels()

    private lateinit var binding: FragmentBreakingNewsBinding

    @Inject
    lateinit var newsArticleAdapter: NewsArticleListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            recyclerView.apply {
                adapter = newsArticleAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.breakingNews.collect {
                    val result = it ?: return@collect

                    swipeRefreshLayout.isRefreshing = result is DataStatus.Loading
                    recyclerView.isVisible = !result.data.isNullOrEmpty()
                    textViewError.isVisible = result.error != null && result.data.isNullOrEmpty()
                    buttonRetry.isVisible = result.error != null && result.data.isNullOrEmpty()
                    textViewError.text = getString(R.string.could_not_refresh, result.error?.localizedMessage ?: getString(R.string.unknown_error_occurred))

                    newsArticleAdapter.submitList(result.data)
                }

            }
        }
    }

}