package com.aerobush.carbtracker.workers

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aerobush.carbtracker.MainActivity
import com.aerobush.carbtracker.R
import com.aerobush.carbtracker.data.CarbTrackerConstants

class WorkerUtils {
    companion object {
        fun makeNormalNotification(title: String, message: String, context: Context) {
            makeNotification(
                channelId = CarbTrackerConstants.NORMAL_CHANNEL_ID,
                title = title,
                message = message,
                notificationId = 1,
                context = context
            )
        }

        fun makeUrgentNotification(title: String, message: String, context: Context) {
            makeNotification(
                channelId = CarbTrackerConstants.URGENT_CHANNEL_ID,
                title = title,
                message = message,
                notificationId = 2,
                context = context
            )
        }

        private fun makeNotification(
            channelId: String,
            title: String,
            message: String,
            notificationId: Int,
            context: Context) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )

            // Create the notification
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)

            // Show the notification
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
}