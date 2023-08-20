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
) : ViewModel() {

    // Объявляем переменные для управления состоянием
    private lateinit var notificationUtils: NotificationUtils // Утилита для управления уведомлениями
    private var timerJob: Job? = null // Job для запуска и остановки таймера

    // Переменные состояния, которые будут доступны извне
    private val _sendNotifications = MutableStateFlow(false)
    val sendNotifications: StateFlow<Boolean> = _sendNotifications

    private val _interval = MutableStateFlow(20)
    val interval: StateFlow<Int> = _interval

    private val _timer = MutableStateFlow(20 * 60)
    val timer: StateFlow<Int> = _timer

    private val notificationMessages = arrayOf(
        "Give your eyes a break! Look away from the screen.",
        "Time for a quick eye rest. Focus on something distant.",
        "Your eyes need a breather. Blink a few times.",
        "Take a moment to relax your eyes. Close them gently.",
        "Give your eyes a break and stretch. Focus on the horizon.",
        "It's time to rest your eyes. Look around and enjoy the view.",
        "Let your eyes rest for a while. Look outside and admire nature.",
        "Pause for a moment and let your eyes rejuvenate. Blink slowly.",
        "Relax your eyes and breathe. Gaze into the distance for a moment.",
        "Your eyes deserve a break. Close them softly and unwind.",
    )

    private val _notificationTimes = MutableStateFlow<List<Long>>(emptyList())
    val notificationTimes: StateFlow<List<Long>> = _notificationTimes

    // Инициализация
    init {
        // Создаем объект notificationUtils с контекстом
        notificationUtils = NotificationUtils
    }

    // Метод для запуска таймера, если необходимо
    fun startTimerIfNeeded() {
        val intervalValue = interval.value
        val sendNotificationsValue = sendNotifications.value

        if (sendNotificationsValue && intervalValue > 0) {
            startTimer(intervalValue)
        } else {
            stopTimer()
        }
    }

    // Остановка таймера
    fun stopTimer() {
        timerJob?.cancel()
    }

    // Запуск таймера с указанным интервалом
    private fun startTimer(interval: Int) {
        stopTimer()

        // Запускаем таймер в корутине
        timerJob = viewModelScope.launch {
            while (_timer.value > 0) {
                delay(1000) // Ждем 1 секунду
                _timer.value--
            }
            // По завершении таймера вызываем уведомление
            showRestReminderNotification()
            // Сбрасываем таймер и запускаем его заново, если уведомления включены
            _timer.value = interval * 60
            startTimerIfNeeded()
        }
    }

    // Показать уведомление о необходимости отдохнуть
    fun showRestReminderNotification() {
        val randomIndex = (0 until notificationMessages.size).random()
        val contentText = notificationMessages[randomIndex]
        notificationUtils.showNotification(context, contentText)

        val currentTime = System.currentTimeMillis()
        addNotificationTime(currentTime)
    }

    // Методы для обновления интервала и включения/выключения уведомлений
    fun setInterval(minutes: Int) {
        _interval.value = minutes
        _timer.value = minutes * 60
    }

    fun setSendNotifications(enabled: Boolean) {
        _sendNotifications.value = enabled
        if (!enabled) {
            _timer.value = interval.value * 60
            stopTimer()
        } else {
            startTimerIfNeeded()
        }
    }

    fun addNotificationTime(time: Long) {
        val updatedList = _notificationTimes.value.toMutableList()
        updatedList.add(time)
        _notificationTimes.value = updatedList
    }

    // Очистка ресурсов при уничтожении ViewModel
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}




