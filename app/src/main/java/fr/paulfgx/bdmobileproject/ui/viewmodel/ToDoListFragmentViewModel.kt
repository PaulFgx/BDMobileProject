package fr.paulfgx.bdmobileproject.ui.viewmodel

import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.fragment.ToDoListFragment
import fr.paulfgx.bdmobileproject.ui.utils.getCurrentDateTime
import fr.paulfgx.bdmobileproject.ui.utils.unAccent
import kotlinx.android.synthetic.main.fragment_todolist.*
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

open class ToDoListFragmentViewModel(
) : ViewModel() {

    val tasksRef = Firebase.database.reference.child("Tasks")

    var completeTaskList = mutableListOf<Task>()
    var completeMapIdToPosition = mutableMapOf<String, Int>()

    var taskList = mutableListOf<Task>()
    var mapIdToPosition = mutableMapOf<String, Int>()

    var currentSearch = ""

    fun writeNewTaskInFirebase(name: String, isSelected: Boolean, onRefresh: OnRefresh) {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteTaskInFirebase(task: Task, onRefresh: OnRefresh) {
        try {
            if (mapIdToPosition.containsKey(task.firebaseId)) {
                val position = mapIdToPosition[task.firebaseId]
                taskList.removeAt(position!!)
                onRefresh()
                tasksRef.child(task.firebaseId).removeValue()
            }
            if (completeMapIdToPosition.containsKey(task.firebaseId)) {
                val position = completeMapIdToPosition[task.firebaseId]
                completeTaskList.removeAt(position!!)
            }
            updateMapWithNewPositions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateTaskInFirebase(task: Task, onRefreshAtPosition: OnRefreshAtPosition) {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun updateMapWithNewPositions() {
        mapIdToPosition = mutableMapOf()
        for (i in 0 until taskList.size) {
            mapIdToPosition[taskList[i].firebaseId] = i
        }
        completeMapIdToPosition = mutableMapOf()
        for (i in 0 until completeTaskList.size) {
            completeMapIdToPosition[completeTaskList[i].firebaseId] = i
        }
    }

    fun getDataFromFirebase(onEventAtPosition: OnEventAtPosition) {
        // Get Data once when opening the application
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseError", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                taskList = mutableListOf()
                val taskMap = dataSnapshot.value as? HashMap<*, *>
                taskMap?.map { entry ->
                    val task = entry.value as HashMap<*, *>
                    val firebaseId = entry.key as String
                    val name = task["name"] as String
                    val isChecked = task["selected"] as Boolean
                    val createdAt = task["createdAt"] as String
                    val updatedAt = task["updatedAt"] as String
                    taskList.add(Task(name, isChecked, createdAt, updatedAt, firebaseId))
                    completeTaskList.add(
                            Task(
                                    name,
                                    isChecked,
                                    createdAt,
                                    updatedAt,
                                    firebaseId
                            )
                    )
                    mapIdToPosition.put(firebaseId, mapIdToPosition.size)
                }
                completeMapIdToPosition = mapIdToPosition
                onEventAtPosition(-1, Event.FIRST_LOADING)
                observeChange { position, event ->
                    when (event) {
                        Event.INSERT -> onEventAtPosition(position, event)
                        Event.UPDATE -> onEventAtPosition(position, event)
                        Event.DELETE -> onEventAtPosition(position, event)
                        else -> throw IllegalStateException("This Should Not Happen")
                    }
                }
            }
        })
    }

    fun observeChange(onEventAtPosition: OnEventAtPosition) {

        tasksRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val task = dataSnapshot.value as HashMap<*, *>
                    val name = task["name"] as String
                    val isChecked = task["selected"] as Boolean
                    val createdAt = task["createdAt"] as String
                    val updatedAt = task["updatedAt"] as String
                    val firebaseId = dataSnapshot.key as String
                    val newTask = Task(name, isChecked, createdAt, updatedAt, firebaseId)
                    if (!mapIdToPosition.containsKey(firebaseId) && newTask.matches(currentSearch)) {
                        taskList.add(newTask)
                        val position = taskList.size - 1
                        mapIdToPosition[firebaseId] = position
                        onEventAtPosition(position, Event.INSERT)
                    }
                    if (!completeMapIdToPosition.containsKey(firebaseId)) {
                        completeTaskList.add(newTask)
                        val position = completeTaskList.size - 1
                        completeMapIdToPosition[firebaseId] = position
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val task = dataSnapshot.value as HashMap<*, *>
                    val name = task["name"] as String
                    val isChecked = task["selected"] as Boolean
                    val createdAt = task["createdAt"] as String
                    val updatedAt = task["updatedAt"] as String
                    val firebaseId = dataSnapshot.key as String
                    val updateTask = Task(name, isChecked, createdAt, updatedAt, firebaseId)
                    if (mapIdToPosition.containsKey(firebaseId)) {
                        val position = mapIdToPosition[firebaseId] as Int
                        val isExpanded = taskList[position].isExpanded
                        updateTask.isExpanded = isExpanded
                        taskList[position] = updateTask
                        onEventAtPosition(position, Event.UPDATE)
                    }
                    if (completeMapIdToPosition.containsKey(firebaseId)) {
                        val position = completeMapIdToPosition[firebaseId]
                        completeTaskList[position!!] = updateTask
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                try {
                    val firebaseId = dataSnapshot.key as String
                    if (mapIdToPosition.containsKey(firebaseId)) {
                        val position = mapIdToPosition[firebaseId] as Int
                        taskList.removeAt(position)
                        updateMapWithNewPositions()
                        onEventAtPosition(position, Event.DELETE)
                    }
                    if (completeMapIdToPosition.containsKey(firebaseId)) {
                        val position = completeMapIdToPosition[firebaseId] as Int
                        completeTaskList.removeAt(position)
                        updateMapWithNewPositions()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    companion object Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToDoListFragmentViewModel() as T
        }
    }
}