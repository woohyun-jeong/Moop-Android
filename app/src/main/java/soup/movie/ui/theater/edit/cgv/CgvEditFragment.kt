package soup.movie.ui.theater.edit.cgv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import soup.movie.data.model.Theater
import soup.movie.databinding.TheaterEditChildFragmentBinding
import soup.movie.ui.theater.edit.TheaterEditChildFragment
import soup.movie.ui.theater.edit.TheaterEditChildListAdapter
import soup.movie.util.observe

class CgvEditFragment : TheaterEditChildFragment() {

    override val title: String = "CGV"

    private val viewModel: CgvEditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = TheaterEditChildFragmentBinding.inflate(inflater, container, false)
        binding.initViewState(viewModel)
        return binding.root
    }

    private fun TheaterEditChildFragmentBinding.initViewState(viewModel: CgvEditViewModel) {
        val listAdapter = TheaterEditChildListAdapter(object : TheaterEditChildListAdapter.Listener {

            override fun add(theater: Theater): Boolean {
                return viewModel.add(theater)
            }

            override fun remove(theater: Theater) {
                viewModel.remove(theater)
            }
        })
        listView.adapter = listAdapter
        viewModel.uiModel.observe(viewLifecycleOwner) {
            listAdapter.submitList(it.areaGroupList, it.selectedTheaterIdSet)
        }
    }
}
