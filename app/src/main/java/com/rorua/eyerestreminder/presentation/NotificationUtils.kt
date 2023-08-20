package com.rorua.eyerestreminder.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {

    private const val CHANNEL_ID = "EyeRestReminderChannel"     // Идентификатор канала уведомлений
    private const val CHANNEL_NAME = "Eye Rest Reminder"        // Имя канала уведомлений
    private const val NOTIFICATION_ID = 1                       // Идентификатор уведомления

    // Создание канала уведомлений (для Android 8.0 и выше)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Показ уведомления
    fun showNotification(context: Context, contentText:String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("Eye Rest")             // Заголовок уведомления
            .setContentText(contentText)             // Текст уведомления
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Приоритет уведомления
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)  // Категория уведомления
            .setAutoCancel(true)                    // Уведомление закроется при нажатии на него
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(NOTIFICATION_ID, notification) // Отправка уведомления
    }
}
