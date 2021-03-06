package soup.movie.ui.theater.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import soup.movie.data.model.Theater
import soup.movie.domain.theater.edit.TheaterEditManager
import soup.movie.ui.base.BaseViewModel
import javax.inject.Inject

class TheaterEditViewModel @Inject constructor(
    private val manager: TheaterEditManager
) : BaseViewModel() {

    private val _contentUiModel = MutableLiveData<TheaterEditContentUiModel>()
    val contentUiModel: LiveData<TheaterEditContentUiModel>
        get() = _contentUiModel

    private val _footerUiModel = MutableLiveData<TheaterEditFooterUiModel>()
    val footerUiModel: LiveData<TheaterEditFooterUiModel>
        get() = _footerUiModel

    init {
        viewModelScope.launch {
            _contentUiModel.value = TheaterEditContentUiModel.LoadingState
            _contentUiModel.value = withContext(Dispatchers.IO) {
                try {
                    manager.loadAsync()
                    TheaterEditContentUiModel.DoneState
                } catch (t: Throwable) {
                    TheaterEditContentUiModel.ErrorState
                }
            }
        }

        manager.asSelectedTheatersSubject()
            .map { TheaterEditFooterUiModel(it) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _footerUiModel.value = it }
            .disposeOnCleared()
    }

    fun onConfirmClicked() {
        manager.save()
    }

    fun remove(theater: Theater) {
        manager.remove(theater)
    }
}
