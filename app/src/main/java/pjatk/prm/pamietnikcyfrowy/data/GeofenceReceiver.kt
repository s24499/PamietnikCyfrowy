package pjatk.prm.pamietnikcyfrowy.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import pjatk.prm.pamietnikcyfrowy.R

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        if (event.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "geofence_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Geofence alerts",
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Jesteś blisko wpisu")
                .setContentText("Dotarłeś do miejsca z zapisanego wpisu.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            notificationManager.notify(1001, notification)
        }
    }
}