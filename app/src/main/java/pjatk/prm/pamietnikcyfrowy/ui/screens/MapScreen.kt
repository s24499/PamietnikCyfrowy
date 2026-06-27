package pjatk.prm.pamietnikcyfrowy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pjatk.prm.pamietnikcyfrowy.R
import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(entries: List<DiaryEntry>, onBackClick: () -> Unit) {
    val locEntries = entries.filter { it.latitude != null && it.longitude != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            stringResource(R.string.desc_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        stringResource(R.string.map_count, locEntries.size),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        Configuration.getInstance().userAgentValue = ctx.packageName
                        MapView(ctx).apply {
                            setMultiTouchControls(true)
                            controller.setZoom(10.5)
                            controller.setCenter(
                                locEntries.firstOrNull()
                                    ?.let { GeoPoint(it.latitude!!, it.longitude!!) } ?: GeoPoint(
                                    52.2297,
                                    21.0122
                                ))
                        }
                    },
                    update = { map ->
                        map.overlays.clear()
                        locEntries.forEach { entry ->
                            map.overlays.add(Marker(map).apply {
                                position = GeoPoint(entry.latitude!!, entry.longitude!!)
                                title = entry.title; snippet = entry.city
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            })
                        }
                        map.invalidate()
                    }
                )
            }
            if (locEntries.isEmpty()) Text(
                stringResource(R.string.map_empty_title),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}