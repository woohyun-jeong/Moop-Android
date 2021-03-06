package soup.movie.data.source

import io.reactivex.Observable
import soup.movie.data.model.response.MovieListResponse

interface MoopDataSource {

    fun getNowList(): Observable<MovieListResponse>

    fun getPlanList(): Observable<MovieListResponse>
}
