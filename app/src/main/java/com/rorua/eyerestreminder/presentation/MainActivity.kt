/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.rorua.eyerestreminder.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleButton
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.rememberScalingLazyListState
import com.rorua.eyerestreminder.presentation.theme.EyeRestReminderTheme
import kotlin.reflect.KFunction1

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: EyeRestReminderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val notificationUtils = NotificationUtils
        notificationUtils.createNotificationChannel(this) // Важно: добавьте эту строку
        viewModel = EyeRestReminderViewModel(applicationContext, notificationUtils)

        setContent {
            WearApp(viewModel, notificationUtils)
        }
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun WearApp(viewModel: EyeRestReminderViewModel, notificationUtils: NotificationUtils) {
    EyeRestReminderTheme {
        EyeRestReminder(
            viewModel = viewModel,
            notificationUtils = notificationUtils,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
        )
    }
}


@Composable
fun EyeRestReminder(
    viewModel: EyeRestReminderViewModel,
    notificationUtils: NotificationUtils,
    modifier: Modifier = Modifier
) {

    val interval by viewModel.interval.collectAsStateWithLifecycle()
    val sendNotifications by viewModel.sendNotifications.collectAsStateWithLifecycle()

    val scalingLazyListState: ScalingLazyListState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
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
                segmented = true
            )
        }
//
//        item {
//            Row(
//                modifier = modifier,
//                horizontalArrangement = Arrangement.Center
//            ) {
//                ToggleButton(
//                    checked = interval == 1,
//                    onCheckedChange = {
//                        viewModel.setInterval(1)
//                    },
//                    enabled = true,
//                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
//                ) {
//                    Text("1")
//                }
//                ToggleButton(
//                    checked = interval == 20,
//                    onCheckedChange = {
//                        viewModel.setInterval(20)
//                    },
//                    enabled = true,
//                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
//                ) {
//                    Text("20")
//                }
//                ToggleButton(
//                    checked = interval == 40,
//                    onCheckedChange = {
//                        viewModel.setInterval(40)
//                    },
//                    enabled = true,
//                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
//                ) {
//                    Text("40")
//                }
//                ToggleButton(
//                    checked = interval == 60,
//                    onCheckedChange = {
//                        viewModel.setInterval(60)
//                    },
//                    enabled = true,
//                    modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
//                ) {
//                    Text(text ="60")
//                }
//            }
//        }

        item {
            ToggleChip(
                label = {
                    Text("Notify", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                secondaryLabel = {
                    Text("Every $interval mins", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                checked = sendNotifications,
                // For Switch  toggle controls the Wear Material UX guidance is to set the
                // unselected toggle control color to ToggleChipDefaults.switchUncheckedIconColor()
                // rather than the default.
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
//
                // Отображаем время до следующего уведомления
                val timer = viewModel.timer.collectAsState()
                Text(text = formatTime(timer.value))
            }
        }
    }
}

// Функция для форматирования времени в формат "мм:сс"
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}

@Composable
fun IntervalButton(interval: Int, onToggleIntervalState: KFunction1<Int, Unit>,) {
    ToggleButton(
        checked = true,
        onCheckedChange = {onToggleIntervalState(interval)},
        enabled = true,
        modifier = Modifier.size(ButtonDefaults.SmallButtonSize)
    ) {
        Text(text = "$interval")
    }
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

//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    val notificationUtils = NotificationUtils
//    val viewModel = EyeRestReminderViewModel(, notificationUtils)
//    WearApp(viewModel, notificationUtils)
//}