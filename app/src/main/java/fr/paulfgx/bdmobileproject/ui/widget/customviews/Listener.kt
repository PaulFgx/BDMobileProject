package fr.paulfgx.bdmobileproject.ui.widget.customviews

import fr.paulfgx.bdmobileproject.data.model.Task

interface ITaskListener {
    fun onRequestAddingTask(name: String, isSelected: Boolean)
    fun onRequestUpdateTask(task: Task, position: Int)
    fun onRequestDeleteTask(task: Task, position: Int)
    fun onCheckedChangeListener(task: Task)
}