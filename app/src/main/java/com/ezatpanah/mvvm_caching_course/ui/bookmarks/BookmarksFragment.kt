package com.ezatpanah.mvvm_caching_course.ui.bookmarks

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ezatpanah.mvvm_caching_course.R
import com.ezatpanah.mvvm_caching_course.adapter.common.NewsArticleListAdapter
import com.ezatpanah.mvvm_caching_course.databinding.FragmentBookmarksBinding
import com.ezatpanah.mvvm_caching_course.databinding.FragmentBreakingNewsBinding
import com.ezatpanah.mvvm_caching_course.ui.MainActivity
import com.ezatpanah.mvvm_caching_course.viewmodel.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarksFragment : Fragment() , MainActivity.OnBottomNavigationFragmentReselectedListener {

    private var currentBinding: FragmentBookmarksBinding? = null
    private val binding get() = currentBinding!!
    private val viewModel: BookmarksViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        currentBinding = FragmentBookmarksBinding.inflate(inflater, container, false)
        return currentBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookmarksAdapter = NewsArticleListAdapter(
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
                adapter = bookmarksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.bookmarks.collect {
                    val bookmarks = it ?: return@collect

                    bookmarksAdapter.submitList(bookmarks)
                    textViewNoBookmarks.isVisible = bookmarks.isEmpty()
                    recyclerView.isVisible = bookmarks.isNotEmpty()
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bookmarks, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_delete_all_bookmarks -> {
                viewModel.onDeleteAllBookmarks()
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