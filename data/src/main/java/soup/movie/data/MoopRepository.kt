package soup.movie.data

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import soup.movie.data.model.Movie
import soup.movie.data.model.MovieId
import soup.movie.data.model.response.CodeResponse
import soup.movie.data.model.response.MovieListResponse
import soup.movie.data.model.response.isStaleness
import soup.movie.data.source.local.LocalMoopDataSource
import soup.movie.data.source.remote.RemoteMoopDataSource
import soup.movie.data.util.SearchHelper

class MoopRepository(
    private val localDataSource: LocalMoopDataSource,
    private val remoteDataSource: RemoteMoopDataSource
) {

    fun getNowList(): Observable<MovieListResponse> {
        return localDataSource.getNowList()
    }

    fun updateNowList(): Completable {
        return localDataSource.findNowMovieList()
            .subscribeOn(Schedulers.io())
            .map { it.isStaleness() }
            .defaultIfEmpty(true)
            .flatMapCompletable {
                if (it) {
                    remoteDataSource.getNowList()
                        .doOnNext { localDataSource.saveNowList(it) }
                        .ignoreElements()
                } else {
                    Completable.complete()
                }
            }
    }

    fun getPlanList(): Observable<MovieListResponse> {
        return localDataSource.getPlanList()
    }

    fun updatePlanList(): Completable {
        return localDataSource.findPlanMovieList()
            .subscribeOn(Schedulers.io())
            .map { it.isStaleness() }
            .defaultIfEmpty(true)
            .flatMapCompletable {
                if (it) {
                    remoteDataSource.getPlanList()
                        .doOnNext { localDataSource.savePlanList(it) }
                        .ignoreElements()
                } else {
                    Completable.complete()
                }
            }
    }

    fun getMovie(movieId: MovieId): Observable<Movie> =
        Observable.merge(
            getNowList().map { it.list },
            getPlanList().map { it.list })
            .flatMapIterable { it }
            .filter { it.isMatchedWith(movieId) }
            .take(1)

    private fun Movie.isMatchedWith(movieId: MovieId): Boolean {
        return id == movieId.id
            || title == movieId.title
            || cgv?.id.isMatched(movieId.cgvId)
            || lotte?.id.isMatched(movieId.lotteId)
            || megabox?.id.isMatched(movieId.megaboxId)
    }

    private fun String?.isMatched(id: String?): Boolean {
        if (this == null || id == null) return false
        return this == id
    }

    suspend fun searchMovie(query: String): List<Movie> {
        return localDataSource.getAllMovieList().asSequence()
            .filter { it.isMatchedWith(query) }
            .toList()
    }

    private fun Movie.isMatchedWith(query: String): Boolean {
        return SearchHelper.matched(title, query)
    }

    suspend fun getCodeList(): CodeResponse {
        return localDataSource.getCodeList()
            ?: remoteDataSource.getCodeList()
                .also(localDataSource::saveCodeList)
    }
}
