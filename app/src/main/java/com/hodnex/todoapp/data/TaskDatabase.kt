package com.hodnex.todoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hodnex.todoapp.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()
            applicationScope.launch {
                dao.insert(Task(name = "Call mom"))
                dao.insert(Task(name = "Swim in sink"))
                dao.insert(Task(name = "Scratch teeth", priority = true))
                dao.insert(Task(name = "Kill pickle", completed = true))
                dao.insert(Task(name = "Eat socks"))
                dao.insert(Task(name = "Resurrect a hamster", priority = true))
                dao.insert(Task(name = "Call Elon Mask"))
                dao.insert(Task(name = "Wait for death", completed = true))
                dao.insert(Task(name = "Become Schwarzenegger"))
                dao.insert(Task(name = "Don't be bald", priority = true))
            }

        }
    }
}