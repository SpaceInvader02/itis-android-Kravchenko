package com.example.homework3

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

enum class NotificationImportance(val channelId: String) {
    MIN("channel_min"),
    DEFAULT("channel_default"),
    HIGH("channel_high"),
}

object ActiveNotificationStore {
    private val notifications = mutableMapOf<Int, String>()

    @Synchronized
    fun add(id: Int, channelId: String) {
        notifications[id] = channelId
    }

    @Synchronized
    fun channelIdOf(id: Int): String? = notifications[id]

    @Synchronized
    fun remove(id: Int) {
        notifications.remove(id)
    }

    @Synchronized
    fun clear() {
        notifications.clear()
    }

    @Synchronized
    fun isNotEmpty(): Boolean = notifications.isNotEmpty()
}

object AppVisibility {
    @Volatile
    var inForeground: Boolean = false
}

object NotificationHelper {
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_NOTIFICATION_TITLE = "extra_notification_title"
    const val EXTRA_NOTIFICATION_TEXT = "extra_notification_text"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(
                NotificationImportance.MIN.channelId,
                context.getString(R.string.channel_min_name),
                NotificationManager.IMPORTANCE_MIN
            ),
            NotificationChannel(
                NotificationImportance.DEFAULT.channelId,
                context.getString(R.string.channel_default_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationImportance.HIGH.channelId,
                context.getString(R.string.channel_high_name),
                NotificationManager.IMPORTANCE_HIGH
            ),
        )
        manager.createNotificationChannels(channels)
    }

    fun show(
        context: Context,
        id: Int,
        title: String,
        text: String,
        ongoing: Boolean,
        importance: NotificationImportance,
    ) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, id)
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_TEXT, text)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, importance.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(ongoing)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
        ActiveNotificationStore.add(id, importance.channelId)
    }

    fun update(context: Context, id: Int, title: String, text: String) {
        val channelId = ActiveNotificationStore.channelIdOf(id)
            ?: NotificationImportance.DEFAULT.channelId
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }

    fun cancelAll(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
        ActiveNotificationStore.clear()
    }
}
