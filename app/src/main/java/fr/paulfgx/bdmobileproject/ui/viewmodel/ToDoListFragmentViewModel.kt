package fr.paulfgx.bdmobileproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.data.singleton.VarGlobal
import fr.paulfgx.bdmobileproject.ui.fragment.ToDoListFragment
import fr.paulfgx.bdmobileproject.ui.utils.unAccent
import kotlinx.coroutines.launch

open class ToDoListFragmentViewModel(
) : ViewModel() {

    fun sortByName(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            VarGlobal.taskList.sortedBy { it.name }
                .toMutableList()
            onFinish()
        }
    }

    fun sortByCreatedAt(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            VarGlobal.taskList.sortedByDescending { it.createdAt }
                .toMutableList()
            onFinish()
        }
    }

    fun sortByUpdatedAt(onFinish: OnFinish) {
        viewModelScope.launch {
            reinitList()
            VarGlobal.taskList.sortedByDescending { it.updatedAt }
                .toMutableList()
            onFinish()
        }
    }

    fun sortByChecked(onFinish: OnFinish) {
        viewModelScope.launch {
            VarGlobal.taskList.sortedByDescending { it.isSelected }
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

    companion object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToDoListFragmentViewModel() as T
        }
    }
}