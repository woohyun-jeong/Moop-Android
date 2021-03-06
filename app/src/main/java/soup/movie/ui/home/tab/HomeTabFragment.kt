package soup.movie.ui.home.tab

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import soup.movie.analytics.EventAnalytics
import soup.movie.data.model.Movie
import soup.movie.databinding.HomeContentsBinding
import soup.movie.ui.base.BaseFragment
import soup.movie.ui.home.HomeFragmentDirections
import soup.movie.ui.home.HomeListAdapter
import soup.movie.ui.home.HomeListScrollEffect
import soup.movie.ui.home.MovieSelectManager
import soup.movie.util.doOnApplyWindowInsets
import soup.movie.util.observe
import soup.movie.util.setOnDebounceClickListener
import javax.inject.Inject

abstract class HomeTabFragment : BaseFragment() {

    @Inject
    lateinit var analytics: EventAnalytics

    private lateinit var binding: HomeContentsBinding
    protected abstract val viewModel: HomeTabViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeContentsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.adaptSystemWindowInset()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initViewState(viewModel)
    }

    private fun HomeContentsBinding.adaptSystemWindowInset() {
        root.doOnApplyWindowInsets { _, windowInsets, initialPadding ->
            listView.updatePadding(
                bottom = initialPadding.bottom + windowInsets.systemWindowInsetBottom
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onDestroyView() {
        binding.listView.setOnTouchListener(null)
        super.onDestroyView()
    }

    private fun HomeContentsBinding.initViewState(viewModel: HomeTabViewModel) {
        val listAdapter = HomeListAdapter(root.context) { movie, sharedElements ->
            analytics.clickMovie()
            MovieSelectManager.select(movie)
            findNavController().navigate(
                HomeFragmentDirections.actionToDetail(),
                ActivityNavigatorExtras(
                    activityOptions = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(requireActivity(), *sharedElements)
                )
            )
        }
        listView.apply {
            adapter = listAdapter
            itemAnimator = FadeInAnimator()
            overScrollMode = View.OVER_SCROLL_NEVER
            setOnTouchListener(HomeListScrollEffect(this))
        }
        errorView.setOnDebounceClickListener {
            viewModel.refresh()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            loadingView.isInProgress = it
        }
        viewModel.isError.observe(viewLifecycleOwner) {
            errorView.isVisible = it
        }
        viewModel.contentsUiModel.observe(viewLifecycleOwner) {
            noItemsView.isVisible = it.movies.isEmpty()
            onUpdateList(listView, it.movies)
        }
    }

    protected open fun onUpdateList(listView: RecyclerView, movies: List<Movie>) {
        listView.run {
            val listAdapter = adapter
            if (listAdapter is HomeListAdapter) {
                val isTopPosition = canScrollVertically(-1).not()
                listAdapter.submitList(movies) {
                    if (movies.isNotEmpty() && isTopPosition) {
                        smoothScrollToPosition(0)
                    }
                }
            }
        }
    }

    fun scrollToTop() {
        if (::binding.isInitialized) {
            binding.listView.scrollToPosition(0)
        }
    }
}
