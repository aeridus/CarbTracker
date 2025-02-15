package com.aerobush.carbtracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aerobush.carbtracker.data.ThemeMode
import com.aerobush.carbtracker.ui.item.CarbTimeItemViewModel
import com.aerobush.carbtracker.ui.theme.CarbTrackerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
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
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val themeUiState = viewModel.themeUiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var hasNotificationPermission = false
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasNotificationPermission = it }
    )

    val currentThemeMode = themeUiState.value.themeMode

    CarbTrackerPanel(
        currentThemeMode,
        onThemeModeClick = {
            coroutineScope.launch {
                viewModel.cycleThemeMode(currentThemeMode)
            }
        },
        uiState.value.dayThresholdHour,
        onHourDecrease = {
            var newValue = uiState.value.dayThresholdHour - 1
            if (newValue < 0) {
                newValue = 23
            }
            coroutineScope.launch {
                viewModel.updateDayThresholdHour(newValue)
            }
        },
        onHourIncrease = {
            var newValue = uiState.value.dayThresholdHour + 1
            if (newValue > 23) {
                newValue = 0
            }
            coroutineScope.launch {
                viewModel.updateDayThresholdHour(newValue)
            }
        },
        uiState.value.totalHours,
        uiState.value.totalMinutes,
        uiState.value.totalCarbServings,
        uiState.value.idealMinCarbServingsPerMeal,
        uiState.value.idealMaxCarbServingsPerMeal,
        uiState.value.idealMinCarbServingsPerWeek,
        uiState.value.idealMaxCarbServingsPerWeek,
        onServingClick = {
            if (!hasNotificationPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Prevent eating if it hasn't been long enough
            if (uiState.value.totalHours >= 3) {
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
    currentThemeMode: ThemeMode,
    onThemeModeClick: () -> Unit,
    dayThresholdHour: Int,
    onHourDecrease: () -> Unit,
    onHourIncrease: () -> Unit,
    totalHours: Int,
    totalMinutes: Int,
    totalCarbServings: Int,
    idealMinCarbServingsPerMeal: Int,
    idealMaxCarbServingsPerMeal: Int,
    idealMinCarbServingsPerWeek: Int,
    idealMaxCarbServingsPerWeek: Int,
    onServingClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .height(64.dp)
        ) {
            SettingButtons(
                currentThemeMode,
                onThemeModeClick,
                dayThresholdHour,
                onHourDecrease,
                onHourIncrease
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            LastMealTime(
                totalHours,
                totalMinutes
            )

            NumberButtonGrid(
                idealMinCarbServingsPerMeal,
                idealMaxCarbServingsPerMeal,
                onServingClick
            )

            WeeklyBudget(
                totalCarbServings,
                idealMinCarbServingsPerWeek,
                idealMaxCarbServingsPerWeek
            )
        }
    }
}

@Composable
fun SettingButtons(
    currentThemeMode: ThemeMode,
    onThemeModeClick: () -> Unit,
    dayThresholdHour: Int,
    onHourDecrease: () -> Unit,
    onHourIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    @DrawableRes var themeModeIcon: Int = R.drawable.cpu
    @StringRes var themeModeIconDescription: Int = R.string.cpu
    when (currentThemeMode) {
        ThemeMode.DARK -> {
            themeModeIcon = R.drawable.moon
            themeModeIconDescription = R.string.moon
        }
        ThemeMode.LIGHT -> {
            themeModeIcon = R.drawable.sun
            themeModeIconDescription = R.string.sun
        }
        else -> { }
    }

    Button(
        onClick = {
            onThemeModeClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.onBackground,
            contentColor = colorScheme.background
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Image(
            painter = painterResource(themeModeIcon),
            contentDescription = stringResource(themeModeIconDescription),
            colorFilter = ColorFilter.tint(colorScheme.background),
            modifier = Modifier
                .height(24.dp)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.sleeping),
            contentDescription = stringResource(R.string.sleeping),
            colorFilter = ColorFilter.tint(colorScheme.onBackground),
            modifier = Modifier
                .height(32.dp)
        )
    }

    Button(
        onClick = {
            onHourDecrease()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
    ) {
        Text(
            text = "—",
            color = colorScheme.onBackground,
            modifier = Modifier
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
    ) {
        Text(
            text = dayThresholdHour.toString(),
            color = colorScheme.onBackground,
            modifier = Modifier
        )
    }

    Button(
        onClick = {
            onHourIncrease()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
    ) {
        Text(
            text = "+",
            color = colorScheme.onBackground,
            modifier = Modifier
        )
    }
}

@Composable
fun LastMealTime(
    totalHours: Int,
    totalMinutes: Int,
    modifier: Modifier = Modifier
) {
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
    if (totalHours > 0) {
        timeSinceLastMeal += if (totalHours == 1) {
            "$totalHours ${stringResource(R.string.hour)} "
        }
        else {
            "$totalHours ${stringResource(R.string.hours)} "
        }
    }
    timeSinceLastMeal += if (totalMinutes == 1) {
        "$totalMinutes ${stringResource(R.string.minute)}"
    }
    else {
        "$totalMinutes ${stringResource(R.string.minutes)}"
    }

    Text(
        text = phrase,
        color = colorScheme.onBackground,
        modifier = modifier
            .padding(8.dp)
    )

    Text(
        text = stringResource(R.string.last_meal_time, timeSinceLastMeal),
        color = colorScheme.onBackground,
        modifier = modifier
            .padding(8.dp)
    )
}

@Composable
fun NumberButtonGrid(
    idealMinCarbServingsPerMeal: Int,
    idealMaxCarbServingsPerMeal: Int,
    onServingClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.select_your_total_carb_servings),
        color = colorScheme.onBackground,
        modifier = Modifier
            .padding(8.dp)
    )

    val choices = (0..8).toList()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
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
                onServingClick = {
                    onServingClick(carbServings)
                }
            )
        }
    }
}

@Composable
fun NumberButton(
    value: Int,
    containerColor: Color,
    contentColor: Color,
    onServingClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onServingClick(value) },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.dp, colorScheme.onBackground),
        modifier = modifier
            .padding(8.dp)
    ) {
        Text(value.toString())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyBudget(
    totalCarbServings: Int,
    idealMinCarbServingsPerWeek: Int,
    idealMaxCarbServingsPerWeek: Int,
    modifier: Modifier = Modifier
) {
    // Figure out where in the weekly carb budget we are
    val maxBudget = idealMaxCarbServingsPerWeek + idealMinCarbServingsPerWeek
    val marginWeight = (idealMinCarbServingsPerWeek * 1f) / maxBudget
    val centerBarWeight = 1f - marginWeight
    val carbBudget = kotlin.math.min(totalCarbServings, maxBudget) * 1f / maxBudget

    Text(
        text = stringResource(R.string.total_carb_servings, totalCarbServings),
        color = colorScheme.onBackground,
        modifier = Modifier
            .padding(8.dp)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(8.dp)
    ) {
        // Generate background colors based on range of ideal carb servings
        Row(
            modifier = Modifier
                .fillMaxSize()
                .border(BorderStroke(1.dp, colorScheme.onBackground))
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
            value = carbBudget,
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

@Preview(showBackground = true)
@Composable
fun CarbTrackerPreviewLight() {
    CarbTrackerTheme {
        CarbTrackerPanel(
            currentThemeMode = ThemeMode.LIGHT,
            onThemeModeClick = {},
            dayThresholdHour = 20,
            onHourDecrease = {},
            onHourIncrease = {},
            totalHours = 2,
            totalMinutes = 5,
            totalCarbServings = 10,
            idealMinCarbServingsPerMeal = 2,
            idealMaxCarbServingsPerMeal = 4,
            idealMinCarbServingsPerWeek = 7,
            idealMaxCarbServingsPerWeek = 14,
            onServingClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CarbTrackerPreviewDark() {
    CarbTrackerTheme(isSystemInDarkTheme = true) {
        CarbTrackerPanel(
            currentThemeMode = ThemeMode.DARK,
            onThemeModeClick = {},
            dayThresholdHour = 20,
            onHourDecrease = {},
            onHourIncrease = {},
            totalHours = 2,
            totalMinutes = 5,
            totalCarbServings = 10,
            idealMinCarbServingsPerMeal = 2,
            idealMaxCarbServingsPerMeal = 4,
            idealMinCarbServingsPerWeek = 7,
            idealMaxCarbServingsPerWeek = 14,
            onServingClick = {}
        )
    }
}