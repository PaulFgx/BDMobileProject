package fr.paulfgx.bdmobileproject.ui.adapter

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.utils.dp
import fr.paulfgx.bdmobileproject.ui.widget.customviews.ITaskListener
import fr.paulfgx.bdmobileproject.ui.widget.holder.ToDoViewHolder

class ToDoListAdapter(
    private val fragment: ITaskListener
) : RecyclerView.Adapter<ToDoViewHolder>(){

    private var _data = emptyList<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ToDoViewHolder {
        return ToDoViewHolder.newInstance(parent)
    }

    override fun getItemCount(): Int = _data.count()

    override fun onBindViewHolder(holder: ToDoViewHolder, position: Int) {
        holder.bind(fragment, _data[position])
    }

    /**
     * Set new data in the list and refresh it
     */
    fun submitList(data: List<Task>) {
        _data = data
        notifyDataSetChanged()
    }

    /**
     * Define how decorate an item
     */
    class OffsetDecoration : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            parent.run {
                outRect.set(
                    dp(1),
                    dp(0),
                    dp(1),
                    dp(0)
                )
            }
        }
    }
}