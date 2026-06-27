package pjatk.prm.pamietnikcyfrowy.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry

class FirestoreDiaryRepository {

    private val collection = FirebaseFirestore.getInstance().collection("entries")

    suspend fun loadEntries(): List<DiaryEntry> = try {
        collection.get().await().documents.mapNotNull {
            it.toObject(FirestoreEntry::class.java)?.toDiaryEntry()
        }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun saveEntry(entry: DiaryEntry) {
        collection.document(entry.id).set(entry.toFirestoreEntry()).await()
    }

    suspend fun updateEntry(entry: DiaryEntry) = saveEntry(entry)

    suspend fun deleteEntry(entryId: String) {
        collection.document(entryId).delete().await()
    }
}