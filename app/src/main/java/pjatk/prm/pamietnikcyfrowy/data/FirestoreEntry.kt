package pjatk.prm.pamietnikcyfrowy.data

import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry

data class FirestoreEntry(
    val id: String = "", val title: String = "", val note: String = "", val city: String = "",
    val photoUri: String? = null, val audioUri: String? = null,
    val latitude: Double? = null, val longitude: Double? = null
)

fun DiaryEntry.toFirestoreEntry() =
    FirestoreEntry(id, title, note, city, photoUri, audioUri, latitude, longitude)

fun FirestoreEntry.toDiaryEntry() =
    DiaryEntry(id, title, note, city, photoUri, audioUri, latitude, longitude)