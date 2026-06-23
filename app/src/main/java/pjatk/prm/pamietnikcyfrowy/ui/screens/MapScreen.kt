package pjatk.prm.pamietnikcyfrowy.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pjatk.prm.pamietnikcyfrowy.model.DiaryEntry

@Composable
fun MapScreen(entries: List<DiaryEntry>) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            Configuration.getInstance().userAgentValue = context.packageName

            MapView(context).apply {
                setMultiTouchControls(true)
                controller.setZoom(10.0)

                val firstPoint = entries.firstOrNull { it.latitude != null && it.longitude != null }
                val center = if (firstPoint != null) {
                    GeoPoint(firstPoint.latitude!!, firstPoint.longitude!!)
                } else {
                    GeoPoint(52.2297, 21.0122)
                }
                controller.setCenter(center)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            entries.forEach { entry ->
                val lat = entry.latitude
                val lon = entry.longitude

                if (lat != null && lon != null) {
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(lat, lon)
                        title = entry.title
                        snippet = entry.city
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(marker)
                }
            }

            mapView.invalidate()
        }
    )
}