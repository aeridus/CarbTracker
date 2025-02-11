package com.aerobush.carbtracker.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carb_time_items")
data class CarbTimeItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long,
    @ColumnInfo(name = "carb_servings")
    val carbServings: Int,
    @ColumnInfo(name = "sent_first_reminder")
    val sentFirstReminder: Boolean,
    @ColumnInfo(name = "sent_second_reminder")
    val sentSecondReminder: Boolean
)