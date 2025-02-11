package com.aerobush.carbtracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aerobush.carbtracker.data.TimeUtils
import com.aerobush.carbtracker.ui.item.CarbTimeItemViewModel
import com.aerobush.carbtracker.ui.theme.CarbTrackerTheme
import kotlinx.coroutines.launch
import java.time.Duration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarbTrackerTheme {
                CarbTracker(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun CarbTracker(
    modifier: Modifier = Modifier,
    viewModel: CarbTimeItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // TODO: show carb budget graphic, run in background, send notifications, save preferences, clear stale items
    val uiState = viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var hasNotificationPermission = false
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasNotificationPermission = it }
    )

    val lastMealTime = uiState.value.lastMealTime

    var totalHours = 24L
    var totalMinutes = 0L
    TimeUtils.getDurationParts(
        startTime = lastMealTime,
        endTime = TimeUtils.getCurrentTime(),
        output =  { hours, minutes ->
            totalHours = hours
            totalMinutes = minutes
        }
    )

    CarbTrackerPanel(
        totalHours,
        totalMinutes,
        uiState.value.totalCarbServings,
        uiState.value.idealMinCarbServingsPerMeal,
        uiState.value.idealMaxCarbServingsPerMeal,
        onClick = {
            if (!hasNotificationPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Prevent eating if it hasn't been long enough
            if (totalHours >= 3) {
                coroutineScope.launch {
                    viewModel.saveCarbTimeItem(it)
                }
            }
        },
        modifier
    )
}

@Composable
fun CarbTrackerPanel(
    totalHours: Long,
    totalMinutes: Long,
    totalCarbServings: Int,
    idealMinCarbServingsPerMeal: Int,
    idealMaxCarbServingsPerMeal: Int,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val choices = (0..8).toList()

    val phrase = if (totalHours >= 4) {
        stringResource(R.string.time_to_eat)
    }
    else if (totalHours >= 3) {
        stringResource(R.string.safe_to_eat)
    }
    else {
        stringResource(R.string.please_wait_before_eating)
    }

    var timeSinceLastMeal = ""
    if (totalHours > 0L)
    {
        if (totalHours == 1L)
        {
            timeSinceLastMeal += "$totalHours ${stringResource(R.string.hour)} "
        }
        else
        {
            timeSinceLastMeal += "$totalHours ${stringResource(R.string.hours)} "
        }
    }
    if (totalMinutes == 1L)
    {
        timeSinceLastMeal += "$totalMinutes ${stringResource(R.string.minute)}"
    }
    else
    {
        timeSinceLastMeal += "$totalMinutes ${stringResource(R.string.minutes)}"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = phrase,
            modifier = Modifier
                .padding(8.dp)
        )

        Text(
            text = stringResource(R.string.last_meal_time, timeSinceLastMeal),
            modifier = Modifier
                .padding(8.dp)
        )

        Text(
            text = stringResource(R.string.select_your_total_carb_servings),
            modifier = Modifier
                .padding(8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
        ) {
            items(choices) { carbServings ->
                var backgroundColor = Color(0f, 0f, 0.8f)
                if (carbServings in idealMinCarbServingsPerMeal..idealMaxCarbServingsPerMeal)
                {
                    backgroundColor = Color(0f, 0.8f, 0f)
                }

                NumberButton(
                    value = carbServings,
                    color = backgroundColor,
                    onClick = {
                        onClick(carbServings)
                    }
                )
            }
        }

        Text(
            text = stringResource(R.string.total_carb_servings, totalCarbServings),
            modifier = Modifier
                .padding(8.dp)
        )
    }
}

@Composable
fun NumberButton(
    value: Int,
    color: Color,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick(value) },
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier
            .padding(8.dp)
    ) {
        Text(value.toString())
    }
}

@Preview(showBackground = true)
@Composable
fun CarbTrackerPreview() {
    CarbTrackerTheme {
        CarbTrackerPanel(
            totalHours = 2,
            totalMinutes = 5,
            totalCarbServings = 0,
            idealMinCarbServingsPerMeal = 2,
            idealMaxCarbServingsPerMeal = 4,
            {}
        )
    }
}