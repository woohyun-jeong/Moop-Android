package soup.movie.ui.theater.sort

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.listener.OnDragStartListener
import androidx.recyclerview.widget.listener.OnItemMoveListener
import androidx.recyclerview.widget.util.SimpleItemTouchHelperCallback
import androidx.transition.TransitionInflater
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.theater_sort_fragment.*
import soup.movie.R
import soup.movie.databinding.TheaterSortFragmentBinding
import soup.movie.ui.BaseFragment
import soup.movie.ui.base.OnBackPressedListener
import soup.movie.util.lazyFast
import soup.movie.util.observe
import soup.movie.util.setOnDebounceClickListener
import java.util.concurrent.TimeUnit

class TheaterSortFragment : BaseFragment(), OnBackPressedListener {

    private val viewModel: TheaterSortViewModel by viewModel()

    private val listAdapter: TheaterSortListAdapter by lazyFast {
        val callback = SimpleItemTouchHelperCallback(object : OnItemMoveListener {
            override fun onItemMove(fromPosition: Int, toPosition: Int) {
                listAdapter.onItemMove(fromPosition, toPosition)
                viewModel.onItemMove(fromPosition, toPosition)
            }
        })
        val itemTouchHelper = ItemTouchHelper(callback).apply {
            attachToRecyclerView(listView)
        }
        TheaterSortListAdapter(object : OnDragStartListener {
            override fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
                itemTouchHelper.startDrag(viewHolder)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postponeEnterTransition(500, TimeUnit.MILLISECONDS)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        sharedElementReturnTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>,
                                             sharedElements: MutableMap<String, View>) {
                sharedElements.clear()
                listView?.run {
                    (0 until childCount)
                        .mapNotNull { getChildAt(it) }
                        .mapNotNull { it.findViewById<Chip>(R.id.theaterChip) }
                        .forEach { sharedElements[it.transitionName] = it }
                }
            }
        })
        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>,
                                             sharedElements: MutableMap<String, View>) {
                sharedElements.clear()
                names.forEach { name ->
                    listView.findViewWithTag<View>(name)?.run {
                        sharedElements[name] = this
                    }
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return TheaterSortFragmentBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        confirmButton.setOnDebounceClickListener {
            onAddItemClick()
        }
        listView.adapter = listAdapter
        viewModel.uiModel.observe(this) {
            render(it)
            //FixMe: find a timing to call startPostponedEnterTransition()
            startPostponedEnterTransition()
        }
    }

    private fun render(uiModel: TheaterSortUiModel) {
        listAdapter.submitList(uiModel.selectedTheaters)
        noItemsView.isVisible = uiModel.selectedTheaters.isEmpty()
    }

    override fun onBackPressed(): Boolean {
        viewModel.saveSnapshot()
        return false
    }

    private fun onAddItemClick() {
        findNavController().navigate(
            TheaterSortFragmentDirections.actionToTheaterEdit(),
            ActivityNavigatorExtras(
                activityOptions = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(requireActivity(), *createSharedElements())
            )
        )
    }

    private fun createSharedElements(): Array<Pair<View, String>> =
        listView?.run {
            (0 until childCount)
                .mapNotNull { getChildAt(it) }
                .mapNotNull { it.findViewById<View>(R.id.theaterChip) }
                .map { Pair(it, it.transitionName) }
                .toTypedArray()
        } ?: emptyArray()
}
