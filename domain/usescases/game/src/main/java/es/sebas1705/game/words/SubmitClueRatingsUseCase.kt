package es.sebas1705.game.words

import es.sebas1705.firestore.datasources.ClueRatingFirestoreDataSource
import es.sebas1705.models.ClueRating
import javax.inject.Inject

class SubmitClueRatingsUseCase @Inject constructor(
    private val dataSource: ClueRatingFirestoreDataSource
) {
    suspend operator fun invoke(ratings: List<ClueRating>): Boolean =
        dataSource.submitRatings(ratings)
}
