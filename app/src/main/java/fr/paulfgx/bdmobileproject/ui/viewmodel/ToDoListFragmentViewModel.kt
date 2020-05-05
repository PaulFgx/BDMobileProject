package fr.paulfgx.bdmobileproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.data.singleton.VarGlobal
import fr.paulfgx.bdmobileproject.ui.fragment.ToDoListFragment
import fr.paulfgx.bdmobileproject.ui.utils.getCurrentDateTime
import fr.paulfgx.bdmobileproject.ui.utils.unAccent
import kotlinx.coroutines.launch

open class ToDoListFragmentViewModel(
) : ViewModel() {

    fun writeNewTaskInFirebase(name: String, isSelected: Boolean, onRefresh: OnRefresh) {
        var idTask = VarGlobal.tasksRef.push().key!!
        val currentTime = getCurrentDateTime()
        val task = Task(name, isSelected, currentTime, currentTime, idTask)
        if (VarGlobal.currentSearch == "" || task.matches(VarGlobal.currentSearch)) {
            VarGlobal.taskList.add(task)
            VarGlobal.mapIdToPosition[idTask] = VarGlobal.taskList.size - 1
            onRefresh()
        }
        VarGlobal.completeTaskList.add(task)
        VarGlobal.completeMapIdToPosition[idTask] = VarGlobal.completeTaskList.size - 1
        VarGlobal.tasksRef.child(idTask).setValue(task)
    }

    fun deleteTaskInFirebase(task: Task, onRefresh: OnRefresh) {
        if (VarGlobal.mapIdToPosition.containsKey(task.firebaseId)) {
            val position = VarGlobal.mapIdToPosition[task.firebaseId]
            try {
                VarGlobal.taskList.removeAt(position!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            onRefresh()
            VarGlobal.tasksRef.child(task.firebaseId).removeValue()
        }
        if (VarGlobal.completeMapIdToPosition.containsKey(task.firebaseId)) {
            val position = VarGlobal.completeMapIdToPosition[task.firebaseId]
            try {
                VarGlobal.completeTaskList.removeAt(position!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        updateMapWithNewPositions()
    }

    fun updateTaskInFirebase(task: Task, onRefreshAtPosition: OnRefreshAtPosition) {
        task.updatedAt = getCurrentDateTime()
        if (VarGlobal.mapIdToPosition.containsKey(task.firebaseId)) {
            var position = VarGlobal.mapIdToPosition[task.firebaseId]
            VarGlobal.taskList[position!!] = task
            onRefreshAtPosition(position)
            VarGlobal.tasksRef.child(task.firebaseId).setValue(task)
        }
        if (VarGlobal.completeMapIdToPosition.containsKey(task.firebaseId)) {
            val position = VarGlobal.completeMapIdToPosition[task.firebaseId]
            VarGlobal.completeTaskList[position!!] = task
        }
    }

    fun sortByName(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            VarGlobal.taskList = VarGlobal.taskList.sortedBy { it.name }
                .toMutableList()
            onFinish()
        }
    }

    fun sortByCreatedAt(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            VarGlobal.taskList = VarGlobal.taskList.sortedByDescending { it.createdAt }
                .toMutableList()
            onFinish()
        }
    }

    fun sortByUpdatedAt(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            VarGlobal.taskList = VarGlobal.taskList.sortedByDescending { it.updatedAt }
                .toMutableList()
            onFinish()
        }
    }

    fun sortByChecked(onFinish: OnFinish) {
        viewModelScope.launch {
            VarGlobal.taskList = VarGlobal.taskList.sortedByDescending { it.isSelected }
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
        VarGlobal.taskList = VarGlobal.completeTaskList.toMutableList()
    }

    private fun updateMapWithNewPositions() {
        VarGlobal.mapIdToPosition = mutableMapOf()
        for (i in 0 until VarGlobal.taskList.size) {
            VarGlobal.mapIdToPosition[VarGlobal.taskList[i].firebaseId] = i
        }
        VarGlobal.completeMapIdToPosition = mutableMapOf()
        for (i in 0 until VarGlobal.completeTaskList.size) {
            VarGlobal.completeMapIdToPosition[VarGlobal.completeTaskList[i].firebaseId] = i
        }
    }

    companion object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToDoListFragmentViewModel() as T
        }
    }
}