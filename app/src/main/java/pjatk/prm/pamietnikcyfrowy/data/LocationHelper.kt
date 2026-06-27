package pjatk.prm.pamietnikcyfrowy.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import java.util.Locale

data class CityLocation(val city: String?, val latitude: Double, val longitude: Double)

class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentCityLocation(): CityLocation? = try {
        fusedLocationClient.lastLocation.await()?.let {
            CityLocation(reverseGeocodeToCity(it.latitude, it.longitude), it.latitude, it.longitude)
        }
    } catch (e: Exception) {
        null
    }

    private fun reverseGeocodeToCity(latitude: Double, longitude: Double): String? = try {
        Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
            ?.firstOrNull()?.run {
            locality ?: subAdminArea ?: adminArea
        }
    } catch (e: Exception) {
        null
    }
}