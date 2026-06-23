package pjatk.prm.pamietnikcyfrowy.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class CityLocation(
    val city: String?,
    val latitude: Double,
    val longitude: Double
)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCityLocation(): CityLocation? {
        return try {
            val location: Location? = fusedLocationClient.lastLocation.await()
            location?.let {
                CityLocation(
                    city = reverseGeocodeToCity(it.latitude, it.longitude),
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun reverseGeocodeToCity(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            addresses?.firstOrNull()?.locality
                ?: addresses?.firstOrNull()?.subAdminArea
                ?: addresses?.firstOrNull()?.adminArea
        } catch (e: Exception) {
            null
        }
    }
}