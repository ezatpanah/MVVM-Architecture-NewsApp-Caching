package com.ezatpanah.mvvm_caching_course.ui.breakingnews

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezatpanah.mvvm_caching_course.R
import com.ezatpanah.mvvm_caching_course.adapter.NewsArticleListAdapter
import com.ezatpanah.mvvm_caching_course.databinding.FragmentBreakingNewsBinding
import com.ezatpanah.mvvm_caching_course.utils.DataStatus
import com.ezatpanah.mvvm_caching_course.utils.exhaustive
import com.ezatpanah.mvvm_caching_course.utils.showSnackbar
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

                    newsArticleAdapter.submitList(result.data) {
                        if (viewModel.pendingScrollToTopAfterRefresh) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterRefresh = false
                        }
                    }

                }

            }

            swipeRefreshLayout.setOnRefreshListener {
                viewModel.onManualRefresh()
            }

            buttonRetry.setOnClickListener {
                viewModel.onManualRefresh()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.events.collect {
                    when (it) {
                        is BreakingNewsViewModel.Event.ShowErrorMessage -> showSnackbar(
                            getString(
                                R.string.could_not_refresh,
                                it.error.localizedMessage
                                    ?: getString(R.string.unknown_error_occurred)
                            )
                        )
                    }.exhaustive
                }
            }

        }

        setHasOptionsMenu(true)

    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_breaking_news, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.onManualRefresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


}