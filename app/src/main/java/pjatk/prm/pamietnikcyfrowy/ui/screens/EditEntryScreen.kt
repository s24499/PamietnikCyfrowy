package pjatk.prm.pamietnikcyfrowy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pjatk.prm.pamietnikcyfrowy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryScreen(
    title: String,
    note: String,
    city: String,
    photoUri: String?,
    audioUri: String?,
    isRecording: Boolean,
    onTitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onGetLocationClick: () -> Unit,
    onPickPhotoClick: () -> Unit,
    onToggleRecordingClick: () -> Unit,
    onPlayAudioClick: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val isSaveEnabled = title.isNotBlank() && note.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            stringResource(R.string.desc_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            stringResource(R.string.desc_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(stringResource(R.string.edit_btn_save)) }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.edit_info),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp)
                )
            }
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.add_field_title)) },
                singleLine = true
            )
            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.add_field_note)) },
                minLines = 5
            )
            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.add_field_city)) },
                singleLine = true
            )

            Button(
                onClick = onGetLocationClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp)); Spacer(
                Modifier.width(8.dp)
            ); Text(stringResource(R.string.edit_btn_location))
            }
            Button(
                onClick = onPickPhotoClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Photo,
                    null,
                    modifier = Modifier.size(18.dp)
                ); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.edit_btn_photo))
            }
            Button(
                onClick = onToggleRecordingClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = if (isRecording) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) else ButtonDefaults.buttonColors()
            ) {
                Icon(
                    Icons.Default.Mic,
                    null,
                    modifier = Modifier.size(18.dp)
                ); Spacer(Modifier.width(8.dp)); Text(stringResource(if (isRecording) R.string.edit_btn_record_stop else R.string.edit_btn_record_start))
            }

            if (photoUri != null) ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }
            if (audioUri != null) Button(
                onClick = { onPlayAudioClick(audioUri) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(18.dp)); Spacer(
                Modifier.width(8.dp)
            ); Text(stringResource(R.string.btn_play_audio))
            }
            if (!isSaveEnabled) Text(
                stringResource(R.string.edit_warning_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}