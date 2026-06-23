package pjatk.prm.pamietnikcyfrowy.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry

class FirestoreDiaryRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("entries")

    suspend fun loadEntries(): List<DiaryEntry> {
        return try {
            val snapshot = collection.get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreEntry::class.java)?.toDiaryEntry()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveEntry(entry: DiaryEntry) {
        collection.document(entry.id).set(entry.toFirestoreEntry()).await()
    }

    suspend fun updateEntry(entry: DiaryEntry) {
        collection.document(entry.id).set(entry.toFirestoreEntry()).await()
    }
}