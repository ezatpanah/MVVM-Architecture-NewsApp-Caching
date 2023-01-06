package com.ezatpanah.mvvm_caching_course.ui.breakingnews

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ezatpanah.mvvm_caching_course.R
import com.ezatpanah.mvvm_caching_course.adapter.common.NewsArticleListAdapter
import com.ezatpanah.mvvm_caching_course.databinding.FragmentBreakingNewsBinding
import com.ezatpanah.mvvm_caching_course.databinding.FragmentSearchNewsBinding
import com.ezatpanah.mvvm_caching_course.ui.MainActivity
import com.ezatpanah.mvvm_caching_course.utils.DataStatus
import com.ezatpanah.mvvm_caching_course.utils.exhaustive
import com.ezatpanah.mvvm_caching_course.utils.showSnackbar
import com.ezatpanah.mvvm_caching_course.viewmodel.BreakingNewsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BreakingNewsFragment : Fragment(),MainActivity.OnBottomNavigationFragmentReselectedListener {

    private val viewModel: BreakingNewsViewModel by viewModels()

    private var currentBinding: FragmentBreakingNewsBinding? = null
    private val binding get() = currentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        currentBinding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        return currentBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsArticleAdapter = NewsArticleListAdapter(
            onItemClick = {
                val uri = Uri.parse(it.url)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                requireActivity().startActivity(intent)
            },
            onBookmarkClick = {
                viewModel.onBookmarkClick(it)
            }
        )

        newsArticleAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


        binding.apply {

            recyclerView.apply {
                adapter = newsArticleAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 0
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

    override fun onBottomNavigationFragmentReselected() {
        binding.recyclerView.scrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentBinding = null
    }

}