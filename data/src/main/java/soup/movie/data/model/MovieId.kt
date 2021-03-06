package soup.movie.data.model

import androidx.annotation.Keep

@Keep
data class MovieId(
    val id: String,
    val title: String,
    val cgvId: CgvMovieId?,
    val lotteId: LotteMovieId?,
    val megaboxId: LotteMovieId?
)
