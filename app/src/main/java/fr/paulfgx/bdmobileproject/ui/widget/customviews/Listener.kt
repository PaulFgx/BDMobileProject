package fr.paulfgx.bdmobileproject.ui.widget.customviews

import fr.paulfgx.bdmobileproject.data.model.Task

interface ITaskListener {
    fun onRequestAddingTask(name: String, isSelected: Boolean)
    fun onRequestUpdateTask(task: Task)
    fun onRequestDeleteTask(task: Task)
    fun onCheckedChangeListener(task: Task)
}