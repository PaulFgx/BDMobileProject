package fr.paulfgx.bdmobileproject.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fr.paulfgx.bdmobileproject.data.model.Task
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

    companion object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToDoListFragmentViewModel() as T
        }
    }
}