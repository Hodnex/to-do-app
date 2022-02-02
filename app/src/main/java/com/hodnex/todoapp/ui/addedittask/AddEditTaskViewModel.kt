package com.hodnex.todoapp.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hodnex.todoapp.data.Task
import com.hodnex.todoapp.data.TaskDao
import com.hodnex.todoapp.ui.ADD_TASK_RESULT_OK
import com.hodnex.todoapp.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")

    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskPriority = state.get<Boolean>("taskPriority") ?: task?.priority ?: false
        set(value) {
            field = value
            state.set("taskPriority", value)
        }

    private val addEditTaskChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskChannel.receiveAsFlow()

    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }

        if (task != null) {
            val updatedTask = task.copy(name = taskName, priority = taskPriority)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, priority = taskPriority)
            addTask(newTask)
        }
    }

    private fun addTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        addEditTaskChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        addEditTaskChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    sealed class AddEditTaskEvent {
        class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }
}