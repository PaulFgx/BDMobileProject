package fr.paulfgx.bdmobileproject.ui.fragment

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.data.singleton.VarGlobal
import fr.paulfgx.bdmobileproject.ui.activity.MainActivity
import fr.paulfgx.bdmobileproject.ui.adapter.ToDoListAdapter
import fr.paulfgx.bdmobileproject.ui.utils.getCurrentDateTime
import fr.paulfgx.bdmobileproject.ui.utils.hideKeyboard
import fr.paulfgx.bdmobileproject.ui.utils.unAccent
import fr.paulfgx.bdmobileproject.ui.viewmodel.ToDoListFragmentViewModel
import fr.paulfgx.bdmobileproject.ui.widget.customviews.AddTaskWidget
import fr.paulfgx.bdmobileproject.ui.widget.customviews.ITaskListener
import kotlinx.android.synthetic.main.fragment_todolist.*

class ToDoListFragment : Fragment(), ITaskListener {

    private lateinit var viewModel: ToDoListFragmentViewModel
    private lateinit var toDoListAdapter: ToDoListAdapter
    private lateinit var searchView: SearchView

    companion object {
        private const val TAG = "ReadValue"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this@ToDoListFragment, ToDoListFragmentViewModel).get()
        setHasOptionsMenu(true)
        VarGlobal.tasksRef.keepSynced(true)
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

        manageAdapterAndRecyclerView()
        manageFabVisibility()
        createFabClickListener()
        getDataFromFirebase()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.sort_menu, menu)

        val manager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val searchItem = menu.findItem(R.id.search_item)
        val searchView = searchItem.actionView as SearchView
        this.searchView = searchView

        searchView.setSearchableInfo(manager.getSearchableInfo(activity?.componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                reinitExpanded()
                VarGlobal.currentSearch = newText
                var list = VarGlobal.completeTaskList.toMutableList()
                viewModel.searchWith(list, newText) {
                    VarGlobal.taskList = it.toMutableList()
                    updateMapWithNewPositions()
                    toDoListAdapter.submitList(VarGlobal.taskList)
                }
                fab.show()
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                fragment_main_layout.hideKeyboard()
                return true
            }
        })
        searchView.setOnCloseListener {
            VarGlobal.currentSearch = ""
            fragment_main_layout.hideKeyboard()
            VarGlobal.taskList = VarGlobal.completeTaskList.toMutableList()
            toDoListAdapter.submitList(VarGlobal.taskList)
            return@setOnCloseListener false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_name -> {
                viewModel.sortByName { refreshAdapter() }
            }
            R.id.item_created_at -> {
                viewModel.sortByCreatedAt { refreshAdapter() }
            }
            R.id.item_updated_at -> {
                viewModel.sortByUpdatedAt { refreshAdapter() }
            }
            R.id.item_checked -> {
                viewModel.sortByChecked { refreshAdapter() }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestAddingTask(name: String, isSelected: Boolean) {
        viewModel.writeNewTaskInFirebase(name, isSelected) {
            toDoListAdapter.notifyItemInserted(toDoListAdapter.itemCount)
        }
    }

    override fun onRequestDeleteTask(task: Task) {
        viewModel.deleteTaskInFirebase(task) {
            toDoListAdapter.notifyDataSetChanged()
            toDoListAdapter.expendedPosition = -1
        }
    }

    override fun onRequestUpdateTask(task: Task) {
        viewModel.updateTaskInFirebase(task) { position ->
            toDoListAdapter.notifyItemChanged(position)
        }
    }

    override fun onCheckedChangeListener(task: Task) {
        viewModel.updateTaskInFirebase(task) {}
    }

    override fun onExpandedChangeListener(newPosition: Int, oldPosition: Int) {
        if (oldPosition != -1) {
            VarGlobal.taskList[oldPosition].isExpanded = false
            toDoListAdapter.notifyItemChanged(oldPosition)
        }
        VarGlobal.taskList[newPosition].isExpanded = true
        toDoListAdapter.notifyItemChanged(newPosition)
    }

    private fun refreshAdapter() {
        updateMapWithNewPositions()
        toDoListAdapter.submitList(VarGlobal.taskList)
    }

    private fun reinitExpanded() {
        if (toDoListAdapter.expendedPosition != -1 && toDoListAdapter.expendedPosition < VarGlobal.taskList.size)
            VarGlobal.taskList[toDoListAdapter.expendedPosition].isExpanded = false
    }

    private fun createFabClickListener() {
        fab.setOnClickListener {
            AddTaskWidget(this)
        }
    }

    private fun manageAdapterAndRecyclerView() {
        toDoListAdapter = ToDoListAdapter(this)
        todo_list_recycler_view.apply {
            adapter = toDoListAdapter
            if (itemDecorationCount == 0) addItemDecoration(ToDoListAdapter.OffsetDecoration())
        }
    }

    private fun manageFabVisibility() {
        todo_list_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) fab.hide() else if (dy < 0) fab.show()
            }
        })
    }

    private fun updateMapWithNewPositions() {
        VarGlobal.mapIdToPosition = mutableMapOf()
        for (i in 0 until VarGlobal.taskList.size) {
            VarGlobal.mapIdToPosition[VarGlobal.taskList[i].firebaseId] = i
        }
        VarGlobal.completeMapIdToPosition = mutableMapOf()
        for (i in 0 until VarGlobal.completeTaskList.size) {
            VarGlobal.completeMapIdToPosition[VarGlobal.completeTaskList[i].firebaseId] = i
        }
    }

    private fun getPositionWithFirebaseId(id: String) = VarGlobal.mapIdToPosition[id]

    //region Firebase Access

    private fun getDataFromFirebase() {
        // Get Data once when opening the application
        VarGlobal.tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d("FirebaseError", error.message)
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                VarGlobal.taskList = mutableListOf()
                val taskMap = dataSnapshot.value as? HashMap<*, *>
                taskMap?.map { entry ->
                    val task = entry.value as HashMap<*, *>
                    val firebaseId = entry.key as String
                    val name = task["name"] as String
                    val isChecked = task["selected"] as Boolean
                    val createdAt = task["createdAt"] as String
                    val updatedAt = task["updatedAt"] as String
                    VarGlobal.taskList.add(Task(name, isChecked, createdAt, updatedAt, firebaseId))
                    VarGlobal.completeTaskList.add(
                        Task(
                            name,
                            isChecked,
                            createdAt,
                            updatedAt,
                            firebaseId
                        )
                    )
                    VarGlobal.mapIdToPosition.put(firebaseId, VarGlobal.mapIdToPosition.size)
                }
                VarGlobal.completeMapIdToPosition = VarGlobal.mapIdToPosition
                progress_bar.visibility = View.GONE
                toDoListAdapter.submitList(VarGlobal.taskList)
                observeChange()
            }
        })
    }

    private fun observeChange() {

        VarGlobal.tasksRef.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)
                val task = dataSnapshot.value as HashMap<*, *>
                val name = task["name"] as String
                val isChecked = task["selected"] as Boolean
                val createdAt = task["createdAt"] as String
                val updatedAt = task["updatedAt"] as String
                val firebaseId = dataSnapshot.key as String
                val newTask = Task(name, isChecked, createdAt, updatedAt, firebaseId)
                if (!VarGlobal.mapIdToPosition.containsKey(firebaseId) && newTask.matches(VarGlobal.currentSearch)) {
                    VarGlobal.taskList.add(newTask)
                    val position = VarGlobal.taskList.size - 1
                    VarGlobal.mapIdToPosition[firebaseId] = position
                    toDoListAdapter.notifyItemInserted(position)
                }
                if (!VarGlobal.completeMapIdToPosition.containsKey(firebaseId)) {
                    VarGlobal.completeTaskList.add(newTask)
                    val position = VarGlobal.completeTaskList.size - 1
                    VarGlobal.completeMapIdToPosition[firebaseId] = position
                }
                fab.show()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")
                val task = dataSnapshot.value as HashMap<*, *>
                val name = task["name"] as String
                val isChecked = task["selected"] as Boolean
                val createdAt = task["createdAt"] as String
                val updatedAt = task["updatedAt"] as String
                val firebaseId = dataSnapshot.key as String
                val updateTask = Task(name, isChecked, createdAt, updatedAt, firebaseId)
                if (VarGlobal.mapIdToPosition.containsKey(firebaseId)) {
                    val position = VarGlobal.mapIdToPosition[firebaseId] as Int
                    val isExpanded = VarGlobal.taskList[position].isExpanded
                    updateTask.isExpanded = isExpanded
                    try {
                        VarGlobal.taskList[position] = updateTask
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    toDoListAdapter.notifyItemChanged(position)
                }
                if (VarGlobal.completeMapIdToPosition.containsKey(firebaseId)) {
                    val position = VarGlobal.completeMapIdToPosition[firebaseId]
                    try {
                        VarGlobal.completeTaskList[position!!] = updateTask
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                fab.show()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)
                val firebaseId = dataSnapshot.key as String
                if (VarGlobal.mapIdToPosition.containsKey(firebaseId)) {
                    val position = getPositionWithFirebaseId(firebaseId) as Int
                    try {
                        VarGlobal.taskList.removeAt(position)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                    toDoListAdapter.notifyDataSetChanged()
                    updateMapWithNewPositions()
                }
                if (VarGlobal.completeMapIdToPosition.containsKey(firebaseId)) {
                    val position = VarGlobal.completeMapIdToPosition[firebaseId] as Int
                    try {
                        VarGlobal.completeTaskList.removeAt(position)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
                fab.show()
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
