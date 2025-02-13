package com.aerobush.carbtracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aerobush.carbtracker.data.TimeUtils
import com.aerobush.carbtracker.ui.item.CarbTimeItemViewModel
import com.aerobush.carbtracker.ui.theme.CarbTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarbTrackerTheme {
                CarbTracker(
                    modifier = Modifier
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
        uiState.value.idealMinCarbServingsPerWeek,
        uiState.value.idealMaxCarbServingsPerWeek,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarbTrackerPanel(
    totalHours: Long,
    totalMinutes: Long,
    totalCarbServings: Int,
    idealMinCarbServingsPerMeal: Int,
    idealMaxCarbServingsPerMeal: Int,
    idealMinCarbServingsPerWeek: Int,
    idealMaxCarbServingsPerWeek: Int,
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

    // Pretty format for time since last meal
    var timeSinceLastMeal = ""
    if (totalHours > 0L)
    {
        timeSinceLastMeal += if (totalHours == 1L) {
            "$totalHours ${stringResource(R.string.hour)} "
        } else {
            "$totalHours ${stringResource(R.string.hours)} "
        }
    }
    timeSinceLastMeal += if (totalMinutes == 1L)
    {
        "$totalMinutes ${stringResource(R.string.minute)}"
    }
    else
    {
        "$totalMinutes ${stringResource(R.string.minutes)}"
    }

    // Figure out where in the weekly carb budget we are
    val maxBudget = idealMaxCarbServingsPerWeek + idealMinCarbServingsPerWeek
    val marginWeight = (idealMinCarbServingsPerWeek * 1f) / maxBudget
    val centerBarWeight = 1f - marginWeight
    val carbBudget = kotlin.math.min(totalCarbServings, maxBudget) *
            1f / maxBudget
    var budgetPosition by remember { mutableFloatStateOf(0f) }
    budgetPosition = carbBudget

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Text(
            text = phrase,
            color = colorScheme.onBackground,
            modifier = Modifier
                .padding(8.dp)
        )

        Text(
            text = stringResource(R.string.last_meal_time, timeSinceLastMeal),
            color = colorScheme.onBackground,
            modifier = Modifier
                .padding(8.dp)
        )

        Text(
            text = stringResource(R.string.select_your_total_carb_servings),
            color = colorScheme.onBackground,
            modifier = Modifier
                .padding(8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
        ) {
            items(choices) { carbServings ->
                var containerColor = colorScheme.secondary
                var contentColor = colorScheme.onSecondary
                if (carbServings in idealMinCarbServingsPerMeal..idealMaxCarbServingsPerMeal)
                {
                    containerColor = colorScheme.primary
                    contentColor = colorScheme.onPrimary
                }

                NumberButton(
                    value = carbServings,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    onClick = {
                        onClick(carbServings)
                    }
                )
            }
        }

        Text(
            text = stringResource(R.string.total_carb_servings, totalCarbServings),
            color = colorScheme.onBackground,
            modifier = Modifier
                .padding(8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(8.dp)
        ) {
            // Generate background colors based on range of ideal carb servings
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(marginWeight / 2f)
                        .background(colorScheme.error)
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(marginWeight / 2f)
                        .background(colorScheme.tertiary)
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(centerBarWeight)
                        .background(colorScheme.primary)
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(marginWeight / 2f)
                        .background(colorScheme.tertiary)
                )

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(marginWeight / 2f)
                        .background(colorScheme.error)
                )
            }

            // We just want to show the thumb, no background
            Slider(
                value = budgetPosition,
                onValueChange = { },
                valueRange = 0f..1f,
                colors = SliderColors(
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent,
                    Color.Transparent),
                thumb = {
                    Image(
                        painter = painterResource(R.drawable.baguette),
                        contentDescription = stringResource(R.string.baguette),
                        modifier = Modifier
                            .rotate(315f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun NumberButton(
    value: Int,
    containerColor: Color,
    contentColor: Color,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onClick(value) },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = modifier
            .padding(8.dp)
    ) {
        Text(value.toString())
    }
}

@Preview(showBackground = true)
@Composable
fun CarbTrackerPreviewLight() {
    CarbTrackerTheme {
        CarbTrackerPanel(
            totalHours = 2,
            totalMinutes = 5,
            totalCarbServings = 10,
            idealMinCarbServingsPerMeal = 2,
            idealMaxCarbServingsPerMeal = 4,
            idealMinCarbServingsPerWeek = 7,
            idealMaxCarbServingsPerWeek = 14,
            {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CarbTrackerPreviewDark() {
    CarbTrackerTheme(darkTheme = true) {
        CarbTrackerPanel(
            totalHours = 2,
            totalMinutes = 5,
            totalCarbServings = 10,
            idealMinCarbServingsPerMeal = 2,
            idealMaxCarbServingsPerMeal = 4,
            idealMinCarbServingsPerWeek = 7,
            idealMaxCarbServingsPerWeek = 14,
            {}
        )
    }
}