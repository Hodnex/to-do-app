package com.hodnex.todoapp.ui.tasks

import android.app.Application
import androidx.lifecycle.*
import com.hodnex.todoapp.data.PreferencesManager
import com.hodnex.todoapp.data.SortOrder
import com.hodnex.todoapp.data.Task
import com.hodnex.todoapp.data.TaskDao
import com.hodnex.todoapp.ui.ADD_TASK_RESULT_OK
import com.hodnex.todoapp.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle,
    application: Application
) : AndroidViewModel(application) {

    val searchQuery = state.getLiveData("searchQuery", "")

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent = taskEventChannel.receiveAsFlow()

    val preferencesFlow = preferencesManager.preferenceFlow

    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreference ->
        Pair(query, filterPreference)
    }.flatMapLatest { (query, filterPreference) ->
        taskDao.getTasks(query, filterPreference.hideCompleted, filterPreference.sortOrder)
    }

    val tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrderPreference(sortOrder, getApplication())
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompletedPreference(hideCompleted, getApplication())
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwipe(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeletedTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConformationMessage("Task Added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConformationMessage("Task Edited")
        }
    }

    private fun showTaskSavedConformationMessage(text: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.ShowTaskSavedConformationMessage(text))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteCompletedScreen)
    }

    sealed class TaskEvent {
        object NavigateToAddTaskScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TaskEvent()
        data class ShowUndoDeletedTaskMessage(val task: Task) : TaskEvent()
        data class ShowTaskSavedConformationMessage(val msg: String) : TaskEvent()
        object NavigateToDeleteCompletedScreen : TaskEvent()
    }
}
