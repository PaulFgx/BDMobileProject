package fr.paulfgx.bdmobileproject.ui.widget.customviews

import fr.paulfgx.bdmobileproject.data.model.Task

interface ITaskListener {
    fun OnRequestAddingTask(toDoItem: Task)
    fun OnRequestUpdateTask(toDoItem: Task, position: Int)
    fun OnRequestDeleteTask(position: Int)
}