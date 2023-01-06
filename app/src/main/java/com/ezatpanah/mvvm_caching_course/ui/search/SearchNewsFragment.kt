package com.ezatpanah.mvvm_caching_course.ui.search

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezatpanah.mvvm_caching_course.R
import com.ezatpanah.mvvm_caching_course.adapter.search.NewsArticleLoadStateAdapter
import com.ezatpanah.mvvm_caching_course.adapter.search.NewsArticlePagingAdapter
import com.ezatpanah.mvvm_caching_course.databinding.FragmentSearchNewsBinding
import com.ezatpanah.mvvm_caching_course.ui.MainActivity
import com.ezatpanah.mvvm_caching_course.utils.onQueryTextSubmit
import com.ezatpanah.mvvm_caching_course.utils.showIfOrInvisible
import com.ezatpanah.mvvm_caching_course.utils.showSnackbar
import com.ezatpanah.mvvm_caching_course.viewmodel.SearchNewsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter

@AndroidEntryPoint
class SearchNewsFragment : Fragment(), MainActivity.OnBottomNavigationFragmentReselectedListener {


    private var currentBinding: FragmentSearchNewsBinding? = null
    private val binding get() = currentBinding!!

    private val viewModel: SearchNewsViewModel by viewModels()
    private lateinit var newsArticleAdapter: NewsArticlePagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        currentBinding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        return currentBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsArticleAdapter = NewsArticlePagingAdapter(
            onItemClick = { article ->
                val uri = Uri.parse(article.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
            },
            onBookmarkClick = { article ->
                viewModel.onBookmarkClick(article)
            }
        )

        binding.apply {
            recyclerView.apply {
                adapter = newsArticleAdapter.withLoadStateFooter(
                    NewsArticleLoadStateAdapter(newsArticleAdapter::retry)
                )
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.searchResults.collectLatest { data ->
                    newsArticleAdapter.submitData(data)
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.hasCurrentQuery.collect { hasCurrentQuery ->
                    textViewInstructions.isVisible = !hasCurrentQuery
                    swipeRefreshLayout.isEnabled = hasCurrentQuery
                    if (!hasCurrentQuery) {
                        recyclerView.isVisible = false
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                newsArticleAdapter.loadStateFlow
                    .distinctUntilChangedBy { it.source.refresh }
                    .filter { it.source.refresh is LoadState.NotLoading }
                    .collect {
                        if (viewModel.pendingScrollToTopAfterNewQuery) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterNewQuery = false
                        }
                        if (viewModel.pendingScrollToTopAfterRefresh && it.mediator?.refresh is LoadState.NotLoading) {
                            recyclerView.scrollToPosition(0)
                            viewModel.pendingScrollToTopAfterRefresh = false
                        }
                    }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                newsArticleAdapter.loadStateFlow
                    .collect { loadState ->
                        when (val refresh = loadState.mediator?.refresh) {
                            is LoadState.Loading -> {
                                textViewError.isVisible = false
                                buttonRetry.isVisible = false
                                swipeRefreshLayout.isRefreshing = true
                                textViewNoResults.isVisible = false

                                recyclerView.showIfOrInvisible {
                                    !viewModel.newQueryInProgress && newsArticleAdapter.itemCount > 0
                                }

                                viewModel.refreshInProgress = true
                                viewModel.pendingScrollToTopAfterRefresh = true
                            }
                            is LoadState.NotLoading -> {
                                textViewError.isVisible = false
                                buttonRetry.isVisible = false
                                swipeRefreshLayout.isRefreshing = false
                                recyclerView.isVisible = newsArticleAdapter.itemCount > 0

                                val noResults =
                                    newsArticleAdapter.itemCount < 1 && loadState.append.endOfPaginationReached
                                            && loadState.source.append.endOfPaginationReached

                                textViewNoResults.isVisible = noResults

                                viewModel.refreshInProgress = false
                                viewModel.newQueryInProgress = false
                            }
                            is LoadState.Error -> {
                                swipeRefreshLayout.isRefreshing = false
                                textViewNoResults.isVisible = false
                                recyclerView.isVisible = newsArticleAdapter.itemCount > 0

                                val noCachedResults =
                                    newsArticleAdapter.itemCount < 1 && loadState.source.append.endOfPaginationReached

                                textViewError.isVisible = noCachedResults
                                buttonRetry.isVisible = noCachedResults

                                val errorMessage = getString(
                                    R.string.could_not_load_search_results,
                                    refresh.error.localizedMessage
                                        ?: getString(R.string.unknown_error_occurred)
                                )
                                textViewError.text = errorMessage

                                if (viewModel.refreshInProgress) {
                                    showSnackbar(errorMessage)
                                }
                                viewModel.refreshInProgress = false
                                viewModel.newQueryInProgress = false
                                viewModel.pendingScrollToTopAfterRefresh = false
                            }
                            else -> {}
                        }
                    }
            }

            swipeRefreshLayout.setOnRefreshListener {
                newsArticleAdapter.refresh()
            }

            buttonRetry.setOnClickListener {
                newsArticleAdapter.retry()
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search_news, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView

        searchView.onQueryTextSubmit {
            viewModel.onSearchQuerySubmit(it)
            searchView.clearFocus()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_refresh -> {
                newsArticleAdapter.refresh()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        currentBinding = null
    }

}