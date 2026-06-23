package pjatk.prm.pamietnikcyfrowy.data

import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry

data class FirestoreEntry(
    val id: String = "",
    val title: String = "",
    val note: String = "",
    val city: String = "",
    val photoUri: String? = null,
    val audioUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

fun DiaryEntry.toFirestoreEntry(): FirestoreEntry {
    return FirestoreEntry(
        id = id,
        title = title,
        note = note,
        city = city,
        photoUri = photoUri,
        audioUri = audioUri,
        latitude = latitude,
        longitude = longitude
    )
}

fun FirestoreEntry.toDiaryEntry(): DiaryEntry {
    return DiaryEntry(
        id = id,
        title = title,
        note = note,
        city = city,
        photoUri = photoUri,
        audioUri = audioUri,
        latitude = latitude,
        longitude = longitude
    )
}