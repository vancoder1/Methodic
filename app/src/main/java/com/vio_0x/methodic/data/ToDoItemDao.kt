package com.vio_0x.methodic.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ToDoItemDao {

    @Query("SELECT * FROM todo_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<ToDoItem>>

    @Query("SELECT * FROM todo_items WHERE id = :id")
    fun getItemById(id: Int): Flow<ToDoItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ToDoItem)

    @Update
    suspend fun updateItem(item: ToDoItem)

    @Delete
    suspend fun deleteItem(item: ToDoItem)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteItemById(id: Int)

    @Query("DELETE FROM todo_items WHERE isCompleted = 1")
    suspend fun deleteCompletedItems()
}