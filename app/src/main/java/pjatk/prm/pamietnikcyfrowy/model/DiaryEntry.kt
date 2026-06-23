package pjatk.prm.pamietnikcyfrowy.model

data class DiaryEntry(
    val id: String,
    val title: String,
    val note: String,
    val city: String,
    val photoUri: String? = null,
    val audioUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)