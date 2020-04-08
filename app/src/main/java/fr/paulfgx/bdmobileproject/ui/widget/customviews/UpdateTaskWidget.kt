package fr.paulfgx.bdmobileproject.ui.widget.customviews

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.utils.hideKeyboard

class UpdateTaskWidget(
    private val task: Task,
    private val fragment: ITaskListener,
    position: Int
) {
    init {
        if (fragment is Fragment) {
            val alertDialog = AlertDialog.Builder(fragment.context)
            alertDialog.setTitle("EDIT TASK")

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
            btnOk.text = "Update"
            val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
            val editext = view.findViewById<EditText>(R.id.edittext)
            editext.setText(task.name)
            val alert = alertDialog.create()
            alert.show()

            btnOk.setOnClickListener {
                if (editext.text.isNotBlank()) {
                    fragment.OnRequestUpdateTask(Task(editext.text.toString(), task.isSelected), position)
                    alert.dismiss()
                    fragment.requireView().hideKeyboard()
                }
            }
            btnCancel.setOnClickListener {
                alert.dismiss()
                fragment.requireView().hideKeyboard()
            }
        }
    }
}