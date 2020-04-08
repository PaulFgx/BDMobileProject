package fr.paulfgx.bdmobileproject.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    private lateinit var toDoListAdapter: ToDoListAdapter

    private var todoList = mutableListOf<Task>()

    companion object {
        private const val TAG = "ReadValue"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        loadAdapter()
    }

    override fun OnRequestAddingTask(toDoItem: Task) {
        val listFirebase = Firebase.database.getReference("Tasks")
        var child = listFirebase.child(toDoItem.name)
        val dictionary: Map<String, Any> = hashMapOf("name" to toDoItem.name, "checked" to toDoItem.isSelected )
        child.setValue(dictionary)
        todoList.add(toDoItem)
        toDoListAdapter.submitList(todoList)
    }

    override fun OnRequestDeleteTask(position: Int) {
        todoList.removeAt(position)
        toDoListAdapter.submitList(todoList)
    }

    override fun OnRequestUpdateTask(toDoItem: Task, position: Int) {
        todoList[position] = toDoItem
        toDoListAdapter.submitList(todoList)
    }

    private fun loadAdapter() {

        val listFirebase = Firebase.database.getReference("Tasks")
        // Read from the database
        listFirebase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds in dataSnapshot.children) {
                    var name: String? = ""
                    var checked: Boolean? = false
                    for (dss in ds.children) {
                        var value = ds.value
                        var key = ds.key
                        if (key == "name")
                            name = value.toString()
                        if (key == "checked")
                            checked = value as Boolean
                    }
                    todoList.add(Task(name!!, checked!!))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })



        toDoListAdapter.submitList(todoList)
    }
}
