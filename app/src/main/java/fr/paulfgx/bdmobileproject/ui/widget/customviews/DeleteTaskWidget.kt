package fr.paulfgx.bdmobileproject.ui.widget.customviews

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.Task

class DeleteTaskWidget(
    private val fragment: ITaskListener,
    task: Task
) {
    init {
        if (fragment is Fragment) {
            val alertDialog = AlertDialog.Builder(fragment.context)
            alertDialog.setTitle("ARE YOU SURE ?")

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
            btnOk.text = "Oui"
            val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
            btnCancel.text = "Non"
            val editext = view.findViewById<EditText>(R.id.edittext)
            editext.visibility = View.GONE
            val alert = alertDialog.create()
            alert.show()

            btnOk.setOnClickListener {
                fragment.onRequestDeleteTask(task)
                alert.dismiss()
            }
            btnCancel.setOnClickListener {
                alert.dismiss()
            }
        }
    }
}

