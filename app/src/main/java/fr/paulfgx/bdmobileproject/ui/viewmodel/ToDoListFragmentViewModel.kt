package fr.paulfgx.bdmobileproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.utils.unAccent
import kotlinx.coroutines.launch

open class ToDoListFragmentViewModel(
) : ViewModel() {

    fun sortByName(listTask: MutableList<Task>, onSuccess: OnSuccess<MutableList<Task>>) {
        viewModelScope.launch {
            listTask.sortedBy { it.name }
                .toMutableList()
                .run(onSuccess)
        }
    }

    fun sortByCreatedAt(listTask: MutableList<Task>, onSuccess: OnSuccess<MutableList<Task>>) {
        viewModelScope.launch {
            listTask.sortedByDescending { it.createdAt }
                .toMutableList()
                .run(onSuccess)
        }
    }

    fun sortByUpdatedAt(listTask: MutableList<Task>, onSuccess: OnSuccess<MutableList<Task>>) {
        viewModelScope.launch {
            listTask.sortedByDescending { it.updatedAt }
                .toMutableList()
                .run(onSuccess)
        }
    }

    fun sortByChecked(listTask: MutableList<Task>, onSuccess: OnSuccess<MutableList<Task>>) {
        viewModelScope.launch {
            listTask.sortedByDescending { it.isSelected }
                .toMutableList()
                .run(onSuccess)
        }
    }

    fun searchWith(listTask: MutableList<Task>, searchValue: String, onSuccess: OnSuccess<MutableList<Task>>) {
        viewModelScope.launch {
            listTask.filter { it.name.contains(searchValue.unAccent(), ignoreCase = true) }
                .toMutableList()
                .run(onSuccess)
        }
    }

    companion object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToDoListFragmentViewModel() as T
        }
    }
}