package pjatk.prm.pamietnikcyfrowy.model

object SampleData {
    val entries = listOf(
        DiaryEntry(
            id = "1",
            title = "Pierwszy wpis",
            note = "To jest przykładowa notatka w pamiętniku.",
            city = "Warszawa",
            latitude = 52.2297,
            longitude = 21.0122
        ),
        DiaryEntry(
            id = "2",
            title = "Spacer po mieście",
            note = "Dodałem zdjęcie i nagranie głosowe.",
            city = "Kraków",
            latitude = 50.0647,
            longitude = 19.9450
        )
    )
}