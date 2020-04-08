package fr.paulfgx.bdmobileproject.ui.widget.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.ToDoItem
import kotlinx.android.synthetic.main.holder_todo.view.*

class ToDoViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(toDoItem: ToDoItem) {

        itemView.tv_nom.text = toDoItem.name
        itemView.checkbox.isChecked = toDoItem.isSelected

        itemView.checkbox.setOnClickListener {
            toDoItem.isSelected = itemView.checkbox.isChecked
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