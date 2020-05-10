package fr.paulfgx.bdmobileproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.utils.getCurrentDateTime
import fr.paulfgx.bdmobileproject.ui.utils.unAccent
import kotlinx.coroutines.launch

open class ToDoListFragmentViewModel(
) : ViewModel() {

    val tasksRef = Firebase.database.reference.child("Tasks")

    var completeTaskList = mutableListOf<Task>()
    var completeMapIdToPosition = mutableMapOf<String, Int>()

    var taskList = mutableListOf<Task>()
    var mapIdToPosition = mutableMapOf<String, Int>()

    var currentSearch = ""

    fun writeNewTaskInFirebase(name: String, isSelected: Boolean, onRefresh: OnRefresh) {
        var idTask = tasksRef.push().key!!
        val currentTime = getCurrentDateTime()
        val task = Task(name, isSelected, currentTime, currentTime, idTask)
        if (currentSearch == "" || task.matches(currentSearch)) {
            taskList.add(task)
            mapIdToPosition[idTask] = taskList.size - 1
            onRefresh()
        }
        completeTaskList.add(task)
        completeMapIdToPosition[idTask] = completeTaskList.size - 1
        tasksRef.child(idTask).setValue(task)
    }

    fun deleteTaskInFirebase(task: Task, onRefresh: OnRefresh) {
        if (mapIdToPosition.containsKey(task.firebaseId)) {
            val position = mapIdToPosition[task.firebaseId]
            try {
                taskList.removeAt(position!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onRefresh()
            tasksRef.child(task.firebaseId).removeValue()
        }
        if (completeMapIdToPosition.containsKey(task.firebaseId)) {
            val position = completeMapIdToPosition[task.firebaseId]
            try {
                completeTaskList.removeAt(position!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        updateMapWithNewPositions()
    }

    fun updateTaskInFirebase(task: Task, onRefreshAtPosition: OnRefreshAtPosition) {
        task.updatedAt = getCurrentDateTime()
        if (mapIdToPosition.containsKey(task.firebaseId)) {
            var position = mapIdToPosition[task.firebaseId]
            taskList[position!!] = task
            onRefreshAtPosition(position)
            tasksRef.child(task.firebaseId).setValue(task)
        }
        if (completeMapIdToPosition.containsKey(task.firebaseId)) {
            val position = completeMapIdToPosition[task.firebaseId]
            completeTaskList[position!!] = task
        }
    }

    fun sortByName(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            taskList = taskList.sortedBy { it.name }
                    .toMutableList()
            onFinish()
        }
    }

    fun sortByCreatedAt(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            taskList = taskList.sortedByDescending { it.createdAt }
                    .toMutableList()
            onFinish()
        }
    }

    fun sortByUpdatedAt(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            taskList = taskList.sortedByDescending { it.updatedAt }
                    .toMutableList()
            onFinish()
        }
    }

    fun sortByChecked(onFinish: OnFinish) {
        viewModelScope.launch {
            taskList = taskList.sortedByDescending { it.isSelected }
                    .toMutableList()
            onFinish()
        }
    }

    fun searchWith(listTask: MutableList<Task>, searchValue: String, onSuccess: OnSuccess<MutableList<Task>>) {
        viewModelScope.launch {
            listTask.filter { it.name.unAccent().contains(searchValue.unAccent(), ignoreCase = true) }
                    .toMutableList()
                    .run(onSuccess)
        }
    }

    private fun reinitList() {
        taskList = completeTaskList.toMutableList()
    }

    private fun updateMapWithNewPositions() {
        mapIdToPosition = mutableMapOf()
        for (i in 0 until taskList.size) {
            mapIdToPosition[taskList[i].firebaseId] = i
        }
        completeMapIdToPosition = mutableMapOf()
        for (i in 0 until completeTaskList.size) {
            completeMapIdToPosition[completeTaskList[i].firebaseId] = i
        }
    }

    companion object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToDoListFragmentViewModel() as T
        }
    }
}