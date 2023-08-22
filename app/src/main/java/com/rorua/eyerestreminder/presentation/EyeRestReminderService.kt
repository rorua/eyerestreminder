package com.rorua.eyerestreminder.presentation

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class EyeRestReminderService : Service() {

    private lateinit var viewModel: EyeRestReminderViewModel
    private lateinit var notificationManager: NotificationManagerCompat

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "EyeRestReminderChannel"
    }

    // Вызывается при создании сервиса
    override fun onCreate() {
        super.onCreate()

        // Инициализируем ViewModel, передавая ей applicationContext
        viewModel = EyeRestReminderViewModel(applicationContext)
    }

    // Вызывается при запуске сервиса
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Проверяем, включены ли уведомления
        if (viewModel.sendNotifications.value) {
            // Запускаем таймер через ViewModel
            startTimer()

            // Создаем уведомление для Foreground Service
            val notification = createForegroundNotification(viewModel.timer.value)

            // Запускаем сервис как Foreground Service с уведомлением
            startForeground(NOTIFICATION_ID, notification)
        } else {
            // Если уведомления выключены, просто останавливаем сервис и убираем Foreground
            stopForeground(true)
            stopSelf()
        }

        // Возвращаем START_STICKY, чтобы сервис перезапускался в случае прекращения его работы
        return START_STICKY
    }


    // Метод для создания уведомления Foreground Service
    private fun createForegroundNotification(timeInSeconds: Int): Notification {
        // Создаем текст уведомления с текущим временем
        val notificationText = formatTime(timeInSeconds)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        return notification
    }

    // Метод для форматирования времени в строку
    private fun formatTime(timeInSeconds: Int): String {
        val minutes = timeInSeconds / 60
        val seconds = timeInSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Вызывается при связывании сервиса с другими компонентами
    override fun onBind(intent: Intent?): IBinder? {
        // В данном случае сервис не поддерживает связывание, поэтому возвращаем null
        return null
    }

    // Вызывается при уничтожении сервиса
    override fun onDestroy() {
        super.onDestroy()

        // Останавливаем таймер через ViewModel
        stopTimer()
    }

    // Запускаем таймер через ViewModel
    private fun startTimer() {
        viewModel.startTimerIfNeeded()
    }

    // Останавливаем таймер через ViewModel
    private fun stopTimer() {
        viewModel.stopTimer()
    }
}
