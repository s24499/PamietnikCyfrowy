package pjatk.prm.pamietnikcyfrowy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry

@Composable
fun HomeScreen(
    entries: List<DiaryEntry>,
    onAddClick: () -> Unit,
    onMapClick: () -> Unit,
    onEntryClick: (DiaryEntry) -> Unit,
    onPlayAudioClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Pamiętnik Cyfrowy",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onAddClick) {
                    Text("Dodaj wpis")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onMapClick) {
                    Text("Pokaż mapę")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEntryClick(entry) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.city,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = entry.note,
                            style = MaterialTheme.typography.bodySmall
                        )

                        if (entry.photoUri != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AsyncImage(
                                model = entry.photoUri,
                                contentDescription = "Zdjęcie wpisu",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (entry.audioUri != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { onPlayAudioClick(entry.audioUri) }) {
                                Text("Odtwórz nagranie")
                            }
                        }
                    }
                }
            }
        }
    }
}