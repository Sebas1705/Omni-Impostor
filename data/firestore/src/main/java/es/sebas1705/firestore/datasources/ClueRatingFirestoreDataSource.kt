package es.sebas1705.firestore.datasources

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.sebas1705.common.managers.ClassLogData
import es.sebas1705.models.ClueRating
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ClueRatingFirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : ClassLogData() {

    companion object {
        private const val COLLECTION = "clue_ratings"
        private const val FIELD_WORD = "word"
        private const val FIELD_CATEGORY = "category"
        private const val FIELD_CLUE = "clue"
        private const val FIELD_POSITIVE = "positive"
        private const val FIELD_NEGATIVE = "negative"
    }

    suspend fun submitRatings(ratings: List<ClueRating>): Boolean {
        if (ratings.isEmpty()) return true
        return runCatching {
            val batch = firestore.batch()
            ratings.forEach { rating ->
                val docId = buildDocId(rating.category, rating.word, rating.clue)
                val ref = firestore.collection(COLLECTION).document(docId)
                val updates = mutableMapOf<String, Any>(
                    FIELD_WORD to rating.word,
                    FIELD_CATEGORY to rating.category,
                    FIELD_CLUE to rating.clue,
                )
                if (rating.isPositive) updates[FIELD_POSITIVE] = FieldValue.increment(1L)
                else updates[FIELD_NEGATIVE] = FieldValue.increment(1L)
                batch.set(ref, updates, SetOptions.merge())
            }
            suspendCancellableCoroutine { cont ->
                batch.commit()
                    .addOnSuccessListener { cont.resume(true) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
        }.getOrElse { e ->
            logW("submitRatings failed: ${e.message}")
            false
        }
    }

    private fun buildDocId(category: String, word: String, clue: String): String =
        "${category}_${word}_${clue}"
            .lowercase()
            .replace(" ", "_")
            .replace("/", "_")
            .take(1500)
}
