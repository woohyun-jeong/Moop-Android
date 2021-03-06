package soup.movie.ui.theme

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import soup.movie.theme.ThemeOptionManager
import soup.movie.ui.base.BaseViewModel
import javax.inject.Inject

class ThemeOptionViewModel @Inject constructor(
    private val themeOptionManager: ThemeOptionManager
) : BaseViewModel() {

    private val _uiModel = MutableLiveData<ThemeOptionUiModel>()
    val uiModel: LiveData<ThemeOptionUiModel>
        get() = _uiModel

    init {
        val options = themeOptionManager.getOptions()
        _uiModel.value = ThemeOptionUiModel(
            themeOptionManager
                .getOptions()
                .map { ThemeOptionItemUiModel(it) }
        )
    }

    fun onItemClick(item: ThemeOptionItemUiModel) {
        themeOptionManager.apply(item.themeOption)
    }
}
