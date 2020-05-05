package fr.paulfgx.bdmobileproject.data.singleton

import fr.paulfgx.bdmobileproject.data.model.Task

object VarGlobal {
    var completeTaskList = mutableListOf<Task>()
    var completeMapIdToPosition = mutableMapOf<String, Int>()

    var taskList = mutableListOf<Task>()
    var mapIdToPosition = mutableMapOf<String, Int>()
}