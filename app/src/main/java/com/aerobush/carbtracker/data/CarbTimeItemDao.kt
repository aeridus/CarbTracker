package com.aerobush.carbtracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarbTimeItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(carbTimeItem: CarbTimeItem)

    @Update
    suspend fun update(carbTimeItem: CarbTimeItem)

    @Delete
    suspend fun delete(carbTimeItem: CarbTimeItem)

    @Query("SELECT * from carb_time_items WHERE id = :id")
    fun getItem(id: Long): Flow<CarbTimeItem>

    @Query("SELECT * from carb_time_items ORDER BY time ASC")
    fun getAllItems(): Flow<List<CarbTimeItem>>

    @Query("SELECT * from carb_time_items WHERE time < :time ORDER BY time ASC")
    fun getStaleItems(time: Long): Flow<List<CarbTimeItem>>

    @Query("SELECT * from carb_time_items WHERE time >= :time ORDER BY time ASC")
    fun getRecentItems(time: Long): Flow<List<CarbTimeItem>>
}