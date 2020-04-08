package fr.paulfgx.bdmobileproject.ui.widget.customviews

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.Task

public class AddTaskWidget(
    private val fragment: IAddTaskListener
) {
    init {
        if (fragment is Fragment) {
            val alertDialog = AlertDialog.Builder(fragment.context)
            alertDialog.setTitle("ADD TASK")

            // Get the LayoutInflater from Context
            val layoutInflater: LayoutInflater = LayoutInflater.from(fragment.context)

            // Inflate the layout using LayoutInflater
            val view: View = layoutInflater.inflate(
                R.layout.add_task_widget, // Custom view/ layout
                null, // Root layout to attach the view
                false // Attach with root layout or not
            )

            alertDialog.setView(view)

            val btnOk = view.findViewById<Button>(R.id.btn_ok)
            val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
            val editext = view.findViewById<EditText>(R.id.edittext)
            val alert = alertDialog.create()
            alert.show()

            btnOk.setOnClickListener {
                if (editext.text.isNotBlank()) {
                    fragment.OnRequestAddingTask(Task(editext.text.toString(), false))
                    alert.dismiss()
                }
            }
            btnCancel.setOnClickListener {
                alert.dismiss()
            }
        }
    }
}

public interface IAddTaskListener {
    fun OnRequestAddingTask(toDoItem: Task)
}