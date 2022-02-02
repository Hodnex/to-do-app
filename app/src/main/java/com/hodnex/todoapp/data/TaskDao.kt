package com.hodnex.todoapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    fun getTasks(
        searchQuery: String,
        hideCompleted: Boolean,
        sortOrder: SortOrder
    ): Flow<List<Task>> =
        when (sortOrder) {
            SortOrder.BY_NAME -> getTasksSortedByName(searchQuery, hideCompleted)
            SortOrder.BY_DATE -> getTasksSortedByDate(searchQuery, hideCompleted)
        }

    @Query("SELECT * FROM task_table WHERE(completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY priority DESC, created")
    fun getTasksSortedByDate(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE(completed != :hideCompleted OR completed = 0) AND name LIKE '%' || :searchQuery || '%' ORDER BY priority DESC, name")
    fun getTasksSortedByName(searchQuery: String, hideCompleted: Boolean): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Update
    suspend fun update(task: Task)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()
}