package fr.paulfgx.bdmobileproject.data.singleton

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fr.paulfgx.bdmobileproject.data.model.Task

object VarGlobal {
    val tasksRef = Firebase.database.reference.child("Tasks")

    var completeTaskList = mutableListOf<Task>()
    var completeMapIdToPosition = mutableMapOf<String, Int>()

    var taskList = mutableListOf<Task>()
    var mapIdToPosition = mutableMapOf<String, Int>()

    var currentSearch = ""
}