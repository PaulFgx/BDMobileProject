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
import fr.paulfgx.bdmobileproject.ui.utils.getCurrentDateTime
import fr.paulfgx.bdmobileproject.ui.widget.customviews.AddTaskWidget
import fr.paulfgx.bdmobileproject.ui.widget.customviews.ITaskListener
import kotlinx.android.synthetic.main.fragment_todolist.*
import kotlinx.android.synthetic.main.fragment_todolist.view.*

class ToDoListFragment : Fragment(), ITaskListener {

    private var mapIdToPosition = mutableMapOf<String, Int>()
    private val tasksRef = Firebase.database.reference.child("Tasks")
    private var taskList = mutableListOf<Task>()
    private lateinit var toDoListAdapter: ToDoListAdapter

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

    override fun onRequestDeleteTask(task: Task) {
        deleteTaskInFirebase(task)
    }

    override fun onRequestUpdateTask(task: Task) {
        updateTaskInFirebase(task)
    }

    override fun onCheckedChangeListener(task: Task) {
        updateTaskInFirebase(task)
    }

    private fun updateMapWithNewPositions() {
        mapIdToPosition = mutableMapOf()
        for (i in 0 until taskList.size) {
            mapIdToPosition[taskList[i].firebaseId] = i
        }
    }

    private fun getPositionWithFirebaseId(id: String) = mapIdToPosition[id]

    //region Firebase Access
    private fun writeNewTaskInFirebase(name: String, isSelected: Boolean) {
        var idTask = tasksRef.push().key!!
        mapIdToPosition[idTask] = mapIdToPosition.size
        val currentTime = getCurrentDateTime()
        val task = Task(name, isSelected, currentTime, currentTime, idTask)
        tasksRef.child(idTask).setValue(task)
        taskList.add(task)
        toDoListAdapter.notifyItemInserted(toDoListAdapter.itemCount)
    }

    private fun updateTaskInFirebase(task: Task) {
        getPositionWithFirebaseId(task.firebaseId)?.let { position ->
            task.updatedAt = getCurrentDateTime()
            tasksRef.child(task.firebaseId).setValue(task)
            taskList[position] = task
            toDoListAdapter.notifyItemChanged(position)
        }
    }

    private fun deleteTaskInFirebase(task: Task) {
        getPositionWithFirebaseId(task.firebaseId)?.let { position ->
            taskList.removeAt(position)
            toDoListAdapter.notifyItemRemoved(position)
            updateMapWithNewPositions()
            tasksRef.child(task.firebaseId).removeValue()
        }
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
                    mapIdToPosition.put(firebaseId, mapIdToPosition.size)
                }
                progress_bar.visibility = View.GONE
                toDoListAdapter.submitList(taskList)
                observeChange()
            }
        })
    }

    private fun observeChange() {

        tasksRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                val task = dataSnapshot.value as HashMap<*, *>
                val name = task["name"] as String
                val isChecked = task["selected"] as Boolean
                val createdAt = task["createdAt"] as String
                val updatedAt = task["updatedAt"] as String
                val firebaseId = dataSnapshot.key as String
                if (!mapIdToPosition.containsKey(firebaseId)) {
                    taskList.add(Task(name, isChecked, createdAt, updatedAt, firebaseId))
                    val position = mapIdToPosition.size
                    mapIdToPosition[firebaseId] = position
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
                if (mapIdToPosition.containsKey(firebaseId)) {
                    val updateTask = Task(name, isChecked, createdAt, updatedAt, firebaseId)
                    val position = mapIdToPosition[firebaseId] as Int
                    taskList[position] = updateTask
                    toDoListAdapter.notifyItemChanged(position)
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
                val firebaseId = dataSnapshot.key as String
                if (mapIdToPosition.containsKey(firebaseId)) {
                    val position = getPositionWithFirebaseId(firebaseId) as Int
                    taskList.removeAt(position)
                    toDoListAdapter.notifyItemRemoved(position)
                    updateMapWithNewPositions()
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException())
            }
        })
    }
    //endregion
}
