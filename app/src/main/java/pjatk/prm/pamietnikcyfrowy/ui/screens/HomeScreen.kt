package pjatk.prm.pamietnikcyfrowy.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pjatk.prm.pamietnikcyfrowy.R
import pjatk.prm.pamietnikcyfrowy.data.mapPreviewUrl
import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    entries: List<DiaryEntry>, onAddClick: () -> Unit, onMapClick: () -> Unit,
    onEntryClick: (DiaryEntry) -> Unit, onPlayAudioClick: (String) -> Unit
) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text(stringResource(R.string.home_title)) }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        stringResource(R.string.home_welcome),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onAddClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text(
                            stringResource(R.string.home_btn_add)
                        )
                        }
                        Button(
                            onClick = onMapClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Map, null); Spacer(Modifier.width(8.dp)); Text(
                            stringResource(R.string.home_btn_map)
                        )
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            stringResource(R.string.home_empty_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.home_empty_desc),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries) { entry ->
                        EntryCard(
                            entry,
                            { onEntryClick(entry) },
                            onPlayAudioClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryCard(entry: DiaryEntry, onClick: () -> Unit, onPlayAudioClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(entry.note, style = MaterialTheme.typography.bodyMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(entry.city.ifBlank { stringResource(R.string.unknown_city) }) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            })
                        if (entry.photoUri != null) AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.home_chip_photo)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Photo,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            })
                        if (entry.audioUri != null) AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.home_chip_audio)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Mic,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                            })
                    }
                }
                if (entry.latitude != null && entry.longitude != null) {
                    AsyncImage(
                        model = mapPreviewUrl(entry.latitude, entry.longitude),
                        contentDescription = null,
                        modifier = Modifier
                            .width(120.dp)
                            .height(90.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            if (entry.photoUri != null) AsyncImage(
                model = entry.photoUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            if (entry.audioUri != null) Button(onClick = { onPlayAudioClick(entry.audioUri) }) {
                Icon(
                    Icons.Default.PlayArrow,
                    null
                ); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.btn_play_audio))
            }
        }
    }
}