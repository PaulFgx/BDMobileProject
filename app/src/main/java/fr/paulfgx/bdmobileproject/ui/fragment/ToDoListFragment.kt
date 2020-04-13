package fr.paulfgx.bdmobileproject.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.activity.MainActivity
import fr.paulfgx.bdmobileproject.ui.adapter.ToDoListAdapter
import fr.paulfgx.bdmobileproject.ui.widget.customviews.AddTaskWidget
import fr.paulfgx.bdmobileproject.ui.widget.customviews.ITaskListener
import kotlinx.android.synthetic.main.fragment_todolist.*
import kotlinx.android.synthetic.main.fragment_todolist.view.*

class ToDoListFragment : Fragment(), ITaskListener {

    private val tasksRef = Firebase.database.reference.child("Tasks")
    var maxId: Long = 0;

    private lateinit var toDoListAdapter: ToDoListAdapter

    private var taskList = mutableListOf<Task>()

    companion object {
        private const val TAG = "ReadValue"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tasksRef.keepSynced(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todolist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.supportActionBar?.apply {
            this.setTitle(R.string.to_do)
            this.setDisplayHomeAsUpEnabled(false)
        }
        // We need to inject the OnUserClickListener in the constructor of the adapter
        toDoListAdapter = ToDoListAdapter(this)
        view.todo_list_recycler_view.apply {
            adapter = toDoListAdapter
            if (itemDecorationCount == 0) addItemDecoration(ToDoListAdapter.OffsetDecoration())
        }

        fab.setOnClickListener {
            AddTaskWidget(this)
        }

        getDataFromFirebase()
    }

    override fun OnRequestAddingTask(task: Task) {
        task.id = "Task"+(maxId + 1)
        writeNewTask(task)
        taskList.add(task)
        toDoListAdapter.submitList(taskList)
    }

    override fun OnRequestDeleteTask(position: Int) {
        taskList.removeAt(position)
        toDoListAdapter.submitList(taskList)
    }

    override fun OnRequestUpdateTask(task: Task, position: Int) {
        updateTask(task)
        taskList[position] = task
        toDoListAdapter.submitList(taskList)
    }

    //region Firebase Access
    private fun writeNewTask(task: Task) {
        tasksRef.child(task.id).setValue(task).addOnCompleteListener {
            maxId++
        }
    }

    private fun updateTask(task: Task) {
        // ref.child("myDb").child("awais@gmailcom").child("leftSpace").setValue("YourDateHere");
        val taskId = task.id
        tasksRef.child(task.id).setValue(task)
    }

    private fun getDataFromFirebase() {
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseError", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                maxId = dataSnapshot.childrenCount
                val taskMap = dataSnapshot.value as? HashMap<Any,Any>
                taskMap?.map { entry ->
                    val task = entry.value as HashMap<Any,Any>
                    val id = entry.key as String
                    val name = task["name"] as String
                    val isChecked = task["selected"] as Boolean
                    taskList.add(Task(name, isChecked, id))
                }
                toDoListAdapter.submitList(taskList)
            }
        })
    }
    //endregion
}
