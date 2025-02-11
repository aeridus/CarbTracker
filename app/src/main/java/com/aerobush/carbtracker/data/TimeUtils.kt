package com.aerobush.carbtracker.data

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class TimeUtils {
    companion object {
        fun getCurrentTime(): OffsetDateTime
        {
            return OffsetDateTime.now(ZoneOffset.UTC)
        }

        fun toEpochMilli(time: OffsetDateTime): Long
        {
            return time.toInstant().toEpochMilli()
        }

        fun toOffsetDataTime(time: Long): OffsetDateTime
        {
            return OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(time),
                ZoneOffset.UTC
            )
        }
    }
}