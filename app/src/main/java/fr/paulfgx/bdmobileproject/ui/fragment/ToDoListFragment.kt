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
import fr.paulfgx.bdmobileproject.data.singletons.MapHolder
import fr.paulfgx.bdmobileproject.data.singletons.getPositionWithFirebaseId
import fr.paulfgx.bdmobileproject.ui.activity.MainActivity
import fr.paulfgx.bdmobileproject.ui.adapter.ToDoListAdapter
import fr.paulfgx.bdmobileproject.ui.utils.getCurrentDateTime
import fr.paulfgx.bdmobileproject.ui.widget.customviews.AddTaskWidget
import fr.paulfgx.bdmobileproject.ui.widget.customviews.ITaskListener
import kotlinx.android.synthetic.main.fragment_todolist.*
import kotlinx.android.synthetic.main.fragment_todolist.view.*

class ToDoListFragment : Fragment(), ITaskListener {

    private val tasksRef = Firebase.database.reference.child("Tasks")
    //var map = mutableMapOf<String, Int>()

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
    }

    override fun onCheckedChangeListener(task: Task) {
        updateTaskInFirebase(task)
    }

    //region Firebase Access
    private fun writeNewTaskInFirebase(name: String, isSelected: Boolean) {
        var idTask = tasksRef.push().key!!
        MapHolder.mapIdToPosition[idTask] = MapHolder.mapIdToPosition.size
        val currentTime = getCurrentDateTime()
        val task = Task(name, isSelected, currentTime, currentTime, idTask)
        tasksRef.child(idTask).setValue(task)
        taskList.add(task)
        toDoListAdapter.notifyItemInserted(toDoListAdapter.itemCount)
    }

    private fun updateTaskInFirebase(task: Task) {
        task.updatedAt = getCurrentDateTime()
        tasksRef.child(task.firebaseId).setValue(task)
        val position = MapHolder.mapIdToPosition[task.firebaseId] as Int
        taskList[position] = task
        toDoListAdapter.notifyItemChanged(position)
    }

    private fun deleteTaskInFirebase(task: Task) {
        tasksRef.child(task.firebaseId).removeValue();
    }

    private fun getDataFromFirebase() {

        // Get Data once when opening the application
        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseError", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val taskMap = dataSnapshot.value as? HashMap<*, *>
                taskMap?.map { entry ->
                    val task = entry.value as HashMap<*, *>
                    val firebaseId = entry.key as String
                    val name = task["name"] as String
                    val isChecked = task["selected"] as Boolean
                    val createdAt = task["createdAt"] as String
                    val updatedAt = task["updatedAt"] as String
                    taskList.add(Task(name, isChecked, createdAt, updatedAt, firebaseId))
                    MapHolder.mapIdToPosition.put(firebaseId, MapHolder.mapIdToPosition.size)
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
                val task = dataSnapshot.value as HashMap<*, *>
                val name = task["name"] as String
                val isChecked = task["selected"] as Boolean
                val createdAt = task["createdAt"] as String
                val updatedAt = task["updatedAt"] as String
                val firebaseId = dataSnapshot.key as String
                if (!MapHolder.mapIdToPosition.containsKey(firebaseId)) {
                    taskList.add(Task(name, isChecked, createdAt, updatedAt, firebaseId))
                    val position = MapHolder.mapIdToPosition.size
                    MapHolder.mapIdToPosition[firebaseId] = position
                    toDoListAdapter.notifyItemInserted(position)
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                val task = dataSnapshot.value as HashMap<*, *>
                val name = task["name"] as String
                val isChecked = task["selected"] as Boolean
                val createdAt = task["createdAt"] as String
                val updatedAt = task["updatedAt"] as String
                val firebaseId = dataSnapshot.key as String
                if (MapHolder.mapIdToPosition.containsKey(firebaseId)) {
                    val updateTask = Task(name, isChecked, createdAt, updatedAt, firebaseId)
                    val position = MapHolder.mapIdToPosition[firebaseId] as Int
                    taskList[position] = updateTask
                    toDoListAdapter.notifyItemChanged(position)
                }

                // ...
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
