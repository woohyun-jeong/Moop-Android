package soup.movie.ui.detail

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import soup.movie.data.model.Movie
import soup.movie.domain.model.screenDays
import soup.movie.ui.EventLiveData
import soup.movie.ui.MutableEventLiveData
import soup.movie.ui.base.BaseViewModel
import soup.movie.ui.home.MovieSelectManager
import soup.movie.util.ImageUriProvider
import soup.movie.util.helper.MM_DD
import soup.movie.util.helper.yesterday
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DetailViewModel @Inject constructor(
    private val imageUriProvider: ImageUriProvider
) : BaseViewModel() {

    private val _headerUiModel = MutableLiveData<HeaderUiModel>()
    val headerUiModel: LiveData<HeaderUiModel>
        get() = _headerUiModel

    private val _contentUiModel = MutableLiveData<ContentUiModel>()
    val contentUiModel: LiveData<ContentUiModel>
        get() = _contentUiModel

    private val _shareAction = MutableEventLiveData<ShareAction>()
    val shareAction: EventLiveData<ShareAction>
        get() = _shareAction

    init {
        _headerUiModel.value = HeaderUiModel(
            movie = MovieSelectManager.getSelectedItem()!!
        )
        MovieSelectManager
            .asObservable()
            .subscribeOn(Schedulers.io())
            .delay(500, TimeUnit.MILLISECONDS)
            .map { it.toContentUiModel() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _contentUiModel.value = it }
            .disposeOnCleared()
    }

    fun requestShareImage(target: ShareTarget, bitmap: Bitmap) {
        imageUriProvider(bitmap)
            .map { ShareAction(target, it, "image/*") }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { _shareAction.event = it }
            .disposeOnCleared()
    }

    private fun Movie.toContentUiModel(): ContentUiModel {
        val items = mutableListOf<ContentItemUiModel>()
        items.add(HeaderItemUiModel)
        kobis?.boxOffice?.run {
            items.add(BoxOfficeItemUiModel(
                rank = rank,
                rankDate = yesterday().MM_DD(),
                audience = audiAcc,
                screenDays = screenDays(),
                rating = naver?.userRating.orEmpty(),
                webLink = naver?.link
            ))
        }
        imdb?.run {
            items.add(ImdbItemUiModel(
                imdb = imdb,
                rottenTomatoes = rt ?: NO_RATING,
                metascore = mc ?: NO_RATING,
                webLink = imdbUrl
            ))
        }
        items.add(CgvItemUiModel(
            movieId = cgv?.id.orEmpty(),
            hasInfo = cgv != null,
            rating = cgv?.egg ?: NO_RATING
        ))
        items.add(LotteItemUiModel(
            movieId = lotte?.id.orEmpty(),
            hasInfo = lotte != null,
            rating = lotte?.star ?: NO_RATING
        ))
        items.add(MegaboxItemUiModel(
            movieId = megabox?.id.orEmpty(),
            hasInfo = megabox != null,
            rating = megabox?.star ?: NO_RATING
        ))
        if (kobis?.boxOffice == null) {
            naver?.run {
                items.add(
                    NaverItemUiModel(
                        rating = userRating,
                        webLink = link
                    )
                )
            }
        }
        val plot = plot.orEmpty()
        if (plot.isNotBlank()) {
            items.add(PlotItemUiModel(plot = plot))
        }
        val kobis = kobis
        if (kobis != null) {
            val persons = mutableListOf<PersonUiModel>()
            persons.addAll(kobis.directors.orEmpty().map {
                PersonUiModel(name = it, cast = "감독", query = "감독 $it")
            })
            persons.addAll(kobis.actors.orEmpty().map {
                val cast = if (it.cast.isEmpty()) "출연" else it.cast
                PersonUiModel(name = it.peopleNm, cast = cast, query = "배우 ${it.peopleNm}")
            })
            if (persons.isNotEmpty()) {
                items.add(CastItemUiModel(persons = persons))
            }
        }
        val trailers = trailers.orEmpty()
        if (trailers.isNotEmpty()) {
            items.add(TrailerHeaderItemUiModel(movieTitle = title))
            items.addAll(trailers.map {
                TrailerItemUiModel(trailer = it)
            })
            items.add(TrailerFooterItemUiModel(movieTitle = title))
        }
        return ContentUiModel(items)
    }

    companion object {

        private const val NO_RATING = "평점없음"
    }
}
