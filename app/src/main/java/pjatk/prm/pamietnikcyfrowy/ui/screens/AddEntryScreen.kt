package pjatk.prm.pamietnikcyfrowy.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun AddEntryScreen(
    title: String,
    note: String,
    city: String,
    photoUri: String?,
    audioUri: String?,
    isRecording: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onGetLocationClick: () -> Unit,
    onPickPhotoClick: () -> Unit,
    onToggleRecordingClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Dodaj wpis",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tytuł") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Treść notatki") },
                minLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = city,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Miasto") },
                readOnly = true,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGetLocationClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pobierz moją lokalizację")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onPickPhotoClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Wybierz zdjęcie")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onToggleRecordingClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isRecording) "Zatrzymaj nagrywanie" else "Nagraj głos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Zdjęcie wpisu",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }

            if (audioUri != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Nagranie zapisane", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zapisz wpis")
            }
        }
    }
}