package com.rorua.eyerestreminder.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.rememberScalingLazyListState
import com.rorua.eyerestreminder.presentation.theme.EyeRestReminderTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: EyeRestReminderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        //startService(Intent(this, EyeRestReminderService::class.java))
//        val serviceIntent = Intent(this, EyeRestReminderService::class.java)
//        startService(serviceIntent);

        viewModel = EyeRestReminderViewModel(applicationContext)
        NotificationUtils.createNotificationChannel(this)

        setContent {
            WearApp(viewModel)
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun WearApp(viewModel: EyeRestReminderViewModel) {
    EyeRestReminderTheme {
        EyeRestReminder(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        )
    }
}


@Composable
fun EyeRestReminder(
    viewModel: EyeRestReminderViewModel,
    modifier: Modifier = Modifier
) {

    val focusRequester = remember {
        FocusRequester()
    }
    val coroutineScope = rememberCoroutineScope()

    val interval by viewModel.interval.collectAsStateWithLifecycle()
    val sendNotifications by viewModel.sendNotifications.collectAsStateWithLifecycle()

    val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = {
            PositionIndicator(
                scalingLazyListState = scalingLazyListState,
                modifier = Modifier
            )
        },
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .onRotaryScrollEvent {
                    coroutineScope.launch {
                        scalingLazyListState.scrollBy(it.verticalScrollPixels)
                    }
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = scalingLazyListState,
        ) {
            item {
                AppName(modifier)
            }

            item {
                InlineSlider(
                    value = interval.toFloat(),
                    onValueChange = { viewModel.setInterval(it.toInt()) },
                    increaseIcon = { Icon(InlineSliderDefaults.Increase, "Increase") },
                    decreaseIcon = { Icon(InlineSliderDefaults.Decrease, "Decrease") },
                    valueRange = 1f..60f,
                    steps = 5,
                    segmented = true,
                    enabled = !sendNotifications
                )
            }

            item {
                ToggleChip(
                    label = {
                        Text("Notify", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    secondaryLabel = {
                        Text("Every $interval min.", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    checked = sendNotifications,
                    colors = ToggleChipDefaults.toggleChipColors(
                        uncheckedToggleControlColor = ToggleChipDefaults.SwitchUncheckedIconColor
                    ),
                    toggleControl = {
                        Switch(
                            checked = sendNotifications,
                            enabled = true,
                            modifier = Modifier.semantics {
                                this.contentDescription =
                                    if (sendNotifications) "On" else "Off"
                            }
                        )
                    },
                    onCheckedChange = { viewModel.setSendNotifications(it) },
                    enabled = true,
                )
            }

            item {
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Отображаем время до следующего уведомления
                    val timer = viewModel.timer.collectAsState()
                    Text(text = formatTime(timer.value))
                }
            }

            item {
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    NotificationTimesList(viewModel)
                }
            }
        }
        LaunchedEffect(Unit){ focusRequester.requestFocus()}
    }


}

// Функция для форматирования времени в формат "мм:сс"
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun AppName(modifier: Modifier) {
    Column(
        modifier = modifier.padding(6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.title3,
            color = MaterialTheme.colors.primary,
            text = "Eye Rest Reminder"
        )
    }
}

@Composable
fun NotificationTimesList(viewModel: EyeRestReminderViewModel) {
    val notificationTimes by viewModel.notificationTimes.collectAsState()

    var showNotifications by remember { mutableStateOf(false) }

    CompactChip(
        onClick = { showNotifications = !showNotifications },
        enabled = true,
        label = {
            Text(
                text = if (showNotifications) "Hide" else "More",
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        colors = ChipDefaults.outlinedChipColors(),
        icon = {}
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (showNotifications) {
        if (notificationTimes.isNotEmpty()) {
            Column {
                Text(
                    text = "Notifications:",
                )
                Spacer(modifier = Modifier.height(8.dp))
                notificationTimes.forEachIndexed { index, notificationTime ->
                    val formattedTime =
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(notificationTime))
                    Text("${index + 1}. $formattedTime")
                }
            }
        } else {
            Column {
                Text("No notifications yet")
            }
        }
    }
}

//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    val viewModel = EyeRestReminderViewModel(Context)
//    WearApp(viewModel)
//}