package soup.movie.domain

import io.reactivex.Observable

interface ResultMapper {

    fun <T> Observable<T>.mapResult(): Observable<Result<T>> {
        return compose { observableSource ->
            observableSource
                .map { Result.Success(it) as Result<T> }
                .onErrorReturn { Result.Failure(it) }
                .startWith(Result.Loading)
        }
    }
}
