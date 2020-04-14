package fr.paulfgx.bdmobileproject.ui.widget.holder

import android.animation.LayoutTransition
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

    fun bind(fragment: ITaskListener, task: Task) {

        itemView.apply {
            tv_nom.text = task.name
            tv_created_at.text = "Created At : "+ task.createdAt
            tv_updated_at.text = "Updated At : "+ task.updatedAt
            checkbox.isChecked = task.isSelected

            container2.visibility = View.GONE
            expand_action.setImageResource(R.drawable.ic_expand_more)

            // Setup Layout Transition
            view_holder_root.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            view_holder_root.layoutTransition.setDuration(300)

            // Click Listeners
            setOnClickListener {
                task.isSelected = !task.isSelected
                checkbox.isChecked = task.isSelected
                fragment.onCheckedChangeListener(task)
            }
            checkbox.setOnClickListener {
                task.isSelected = itemView.checkbox.isChecked
                fragment.onCheckedChangeListener(task)
            }
            expand_action.setOnClickListener {
                if (container2.visibility == View.GONE) {
                    expand_action.setImageResource(R.drawable.ic_expand_less)
                    container2.visibility = View.VISIBLE
                }
                else {
                    container2.visibility = View.GONE
                    expand_action.setImageResource(R.drawable.ic_expand_more)
                }
            }
            delete.setOnClickListener {
                DeleteTaskWidget(fragment, task)
            }
            update.setOnClickListener {
                UpdateTaskWidget(fragment, task)
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