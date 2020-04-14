package fr.paulfgx.bdmobileproject.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
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
    var map = mutableMapOf<String, Int>()

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

    override fun onRequestAddingTask(name: String, isSelected: Boolean) {
        writeNewTaskInFirebase(name, isSelected)
    }

    override fun onRequestDeleteTask(task: Task, position: Int) {
        deleteTaskInFirebase(task)
        //taskList.removeAt(position)
        //toDoListAdapter.submitList(taskList)
    }

    override fun onRequestUpdateTask(task: Task, position: Int) {
        updateTaskInFirebase(task)
        //taskList[position] = task
        //toDoListAdapter.submitList(taskList)
    }

    override fun onCheckedChangeListener(task: Task) {
        updateTaskInFirebase(task)
    }

    //region Firebase Access
    private fun writeNewTaskInFirebase(name: String, isSelected: Boolean) {
        var idTask = tasksRef.push().key!!
        map.put(idTask, map.size - 1)
        val task = Task(name, isSelected, idTask)
        tasksRef.child(idTask).setValue(task)
        taskList.add(task)
        toDoListAdapter.notifyItemInserted(toDoListAdapter.itemCount)
    }

    private fun updateTaskInFirebase(task: Task) {
        tasksRef.child(task.firebaseId).setValue(task)
    }

    private fun deleteTaskInFirebase(task: Task) {
        tasksRef.child(task.firebaseId).removeValue();
    }

    private fun getDataFromFirebase() {

        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseError", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val taskMap = dataSnapshot.value as? HashMap<Any, Any>
                taskMap?.map { entry ->
                    val task = entry.value as HashMap<Any, Any>
                    val firebaseId = entry.key as String
                    val name = task["name"] as String
                    val isChecked = task["selected"] as Boolean
                    taskList.add(Task(name, isChecked, firebaseId))
                    map.put(firebaseId, map.size - 1)
                }
                toDoListAdapter.submitList(taskList)

                observeChange()
            }
        })
    }

    private fun observeChange() {
        tasksRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                // A new task has been added, add it to the displayed list
                val task = dataSnapshot.value as HashMap<Any, Any>
                val name = task["name"] as String
                val isChecked = task["selected"] as Boolean
                val firebaseId = dataSnapshot.key
                if (!map.containsKey(firebaseId)) {
                    taskList.add(Task(name, isChecked, firebaseId!!))
                    toDoListAdapter.notifyItemInserted(toDoListAdapter.itemCount)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                /*Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                val newComment = dataSnapshot.getValue<Comment>()
                val commentKey = dataSnapshot.key

                // ...*/
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                val commentKey = dataSnapshot.key

                // ...
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.

                // val movedComment = dataSnapshot.getValue<Comment>()
                val commentKey = dataSnapshot.key

                // ...
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException())

                //Toast.makeText(context, "Failed to load comments.",
                //  Toast.LENGTH_SHORT).show()
            }
        })
    }
    //endregion
}
