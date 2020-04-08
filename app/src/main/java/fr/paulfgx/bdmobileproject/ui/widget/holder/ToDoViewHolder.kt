package fr.paulfgx.bdmobileproject.ui.widget.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.widget.customviews.DeleteTaskWidget
import fr.paulfgx.bdmobileproject.ui.widget.customviews.ITaskListener
import kotlinx.android.synthetic.main.holder_todo.view.*

class ToDoViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(fragment: ITaskListener, toDoItem: Task, position: Int) {

        itemView.apply {
            val context = itemView.context
            tv_nom.text = toDoItem.name
            setOnClickListener {
                toDoItem.isSelected = !toDoItem.isSelected
                checkbox.isChecked = toDoItem.isSelected
            }
            checkbox.setOnClickListener {
                toDoItem.isSelected = itemView.checkbox.isChecked
            }
            container2.visibility = View.GONE
            expand_action.setOnClickListener {
                if (container2.visibility == View.GONE)
                    container2.visibility = View.VISIBLE
                else
                    container2.visibility = View.GONE
            }
            delete.setOnClickListener {
                DeleteTaskWidget(fragment, position)
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