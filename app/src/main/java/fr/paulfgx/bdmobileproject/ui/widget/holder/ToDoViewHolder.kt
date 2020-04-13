package fr.paulfgx.bdmobileproject.ui.widget.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.widget.customviews.DeleteTaskWidget
import fr.paulfgx.bdmobileproject.ui.widget.customviews.ITaskListener
import fr.paulfgx.bdmobileproject.ui.widget.customviews.UpdateTaskWidget
import kotlinx.android.synthetic.main.holder_todo.view.*

class ToDoViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(fragment: ITaskListener, task: Task, position: Int) {

        itemView.apply {
            val context = itemView.context
            tv_nom.text = task.name
            checkbox.isChecked = task.isSelected
            setOnClickListener {
                task.isSelected = !task.isSelected
                checkbox.isChecked = task.isSelected
            }
            checkbox.setOnClickListener {
                task.isSelected = itemView.checkbox.isChecked
                fragment.onCheckedChangeListener(task)
            }
            container2.visibility = View.GONE
            expand_action.setOnClickListener {
                if (container2.visibility == View.GONE)
                    container2.visibility = View.VISIBLE
                else
                    container2.visibility = View.GONE
            }
            delete.setOnClickListener {
                DeleteTaskWidget(fragment, task, position)
            }
            update.setOnClickListener {
                UpdateTaskWidget(fragment, task, position)
            }
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup): ToDoViewHolder {
            return ToDoViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.holder_todo,
                    parent,
                    false
                )
            )
        }
    }
}