package soup.movie.data.source.local

import io.reactivex.Maybe
import io.reactivex.Observable
import soup.movie.data.model.Movie
import soup.movie.data.model.response.CachedMovieList
import soup.movie.data.model.response.CachedMovieList.Companion.TYPE_NOW
import soup.movie.data.model.response.CachedMovieList.Companion.TYPE_PLAN
import soup.movie.data.model.response.CodeResponse
import soup.movie.data.model.response.MovieListResponse
import soup.movie.data.source.MoopDataSource

class LocalMoopDataSource(
    private val moopDao: MoopDao
) : MoopDataSource {

    private var codeResponse: CodeResponse? = null

    fun saveNowList(response: MovieListResponse) {
        saveMovieListAs(TYPE_NOW, response)
    }

    override fun getNowList(): Observable<MovieListResponse> {
        return getMovieListAs(TYPE_NOW)
    }

    fun savePlanList(response: MovieListResponse) {
        saveMovieListAs(TYPE_PLAN, response)
    }

    override fun getPlanList(): Observable<MovieListResponse> {
        return getMovieListAs(TYPE_PLAN)
    }

    private fun saveMovieListAs(type: String, response: MovieListResponse) {
        moopDao.insert(CachedMovieList(type, response.lastUpdateTime, response.list))
    }

    private fun getMovieListAs(type: String): Observable<MovieListResponse> {
        return moopDao.getMovieListByType(type)
            .onErrorReturn { CachedMovieList.empty(type) }
            .map { MovieListResponse(it.lastUpdateTime, it.list) }
            .toObservable()
    }

    fun findNowMovieList() : Maybe<MovieListResponse> {
        return findMovieListAs(TYPE_NOW)
    }

    fun findPlanMovieList() : Maybe<MovieListResponse> {
        return findMovieListAs(TYPE_PLAN)
    }

    private fun findMovieListAs(type: String): Maybe<MovieListResponse> {
        return moopDao.findByType(type)
            .map { MovieListResponse(it.lastUpdateTime, it.list) }
    }

    suspend fun getAllMovieList(): List<Movie> {
        return getNowMovieList() + getPlanMovieList()
    }

    private suspend fun getNowMovieList(): List<Movie> {
        return getMovieListOf(TYPE_NOW)
    }

    private suspend fun getPlanMovieList(): List<Movie> {
        return getMovieListOf(TYPE_PLAN)
    }

    private suspend fun getMovieListOf(type: String): List<Movie> {
        return try {
            moopDao.getMovieListOf(type).list
        } catch (t: Throwable) {
            emptyList()
        }
    }

    fun saveCodeList(response: CodeResponse) {
        codeResponse = response
    }

    fun getCodeList(): CodeResponse? {
        return codeResponse
    }
}
