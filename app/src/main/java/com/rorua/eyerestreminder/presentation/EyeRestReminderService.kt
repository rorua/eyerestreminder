package com.rorua.eyerestreminder.presentation

import android.app.Service
import android.content.Intent
import android.os.IBinder

class EyeRestReminderService : Service() {

    private lateinit var viewModel: EyeRestReminderViewModel

    // Вызывается при создании сервиса
    override fun onCreate() {
        super.onCreate()

        // Инициализируем ViewModel, передавая ей applicationContext
        viewModel = EyeRestReminderViewModel(applicationContext)
    }

    // Вызывается при запуске сервиса
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Запускаем таймер через ViewModel
        startTimer()

        // Возвращаем START_STICKY, чтобы сервис перезапускался в случае прекращения его работы
        return START_STICKY
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
