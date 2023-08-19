package com.rorua.eyerestreminder.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class EyeRestReminderViewModel(
    private val context: Context,
    private val notificationUtils: NotificationUtils
) : ViewModel() {



    private var timerJob: Job? = null


    // Переменные для управления состоянием:
    // Храним состояние отправки уведомлений (включено/выключено)
    private val _sendNotifications = MutableStateFlow(false)
    val sendNotifications: StateFlow<Boolean> = _sendNotifications

    // Храним интервал между уведомлениями (в минутах)
    private val _interval = MutableStateFlow(20)
    val interval: StateFlow<Int> = _interval

    // Храним таймер, отсчитывающий время до следующего уведомления
    private val _timer = MutableStateFlow(20 * 60)
    val timer: StateFlow<Int> = _timer

    // Методы для обновления переменных:
    // Установка интервала между уведомлениями
    fun setInterval(minutes: Int) {
        _interval.value = minutes
        _timer.value = minutes * 60
    }

    // Включение/выключение отправки уведомлений
    fun setSendNotifications(enabled: Boolean) {
        _sendNotifications.value = enabled
        if (!enabled) {
            _timer.value = interval.value * 60 // Сбросить таймер на дефолтное значение
            stopTimer()
        } else {
            startTimerIfNeeded()
        }
    }


    // Запуск таймера при необходимости
    private fun startTimerIfNeeded() {
        val intervalValue = interval.value
        val sendNotificationsValue = sendNotifications.value

        if (sendNotificationsValue && intervalValue > 0) {
            startTimer(intervalValue, ::onTimerTick, ::onTimerFinished)
        } else {
            stopTimer()
        }
    }

    // Запуск таймера
    private fun startTimer(interval: Int, onTick: () -> Unit, onFinished: () -> Unit) {
        stopTimer()

        timerJob = viewModelScope.launch {
            while (_timer.value > 0) {
                delay(1000) // Подождать 1 секунду
                _timer.value--
                onTick()
            }
            onFinished()
        }
    }

    // Остановка таймера
    private fun stopTimer() {
        timerJob?.cancel()
    }

    // Действия на каждом тике таймера (каждую секунду)
    private fun onTimerTick() {
        // Вызов метода для обновления интерфейса, например, для обновления времени
        // Например: updateTimerDisplay()
    }

    // Действия при завершении таймера
    private fun onTimerFinished() {
        // В этом месте можно вызвать метод для показа уведомления о необходимости отдохнуть
        // Например: showRestReminderNotification()
        showRestReminderNotification()
        // Также можно сбросить таймер обратно на исходное значение
        _timer.value = interval.value * 60

        // Запустить таймер снова, если уведомления включены
        startTimerIfNeeded()
    }

    // Отправить уведомление о необходимости отдохнуть
    fun showRestReminderNotification() {
        notificationUtils.showNotification(
            context,
            "Time to rest your eyes!"
        )
    }



    // Очистка ресурсов при уничтожении ViewModel
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}



