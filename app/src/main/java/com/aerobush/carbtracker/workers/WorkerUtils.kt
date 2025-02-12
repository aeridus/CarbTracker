package com.aerobush.carbtracker.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aerobush.carbtracker.R
import com.aerobush.carbtracker.data.CarbTrackerConstants

class WorkerUtils {
    companion object {
        fun makeNormalNotification(title: String, message: String, context: Context) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            // Create the notification
            val builder = NotificationCompat.Builder(context, CarbTrackerConstants.NORMAL_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)

            // Show the notification
            NotificationManagerCompat.from(context).notify(1, builder.build())
        }

        fun makeUrgentNotification(title: String, message: String, context: Context) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            // Create the notification
            val builder = NotificationCompat.Builder(context, CarbTrackerConstants.URGENT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)

            // Show the notification
            NotificationManagerCompat.from(context).notify(2, builder.build())
        }
    }
}