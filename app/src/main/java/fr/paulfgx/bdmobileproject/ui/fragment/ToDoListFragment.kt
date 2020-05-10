package fr.paulfgx.bdmobileproject.ui.fragment

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.RecyclerView
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.Task
import fr.paulfgx.bdmobileproject.ui.activity.MainActivity
import fr.paulfgx.bdmobileproject.ui.adapter.ToDoListAdapter
import fr.paulfgx.bdmobileproject.ui.utils.hideKeyboard
import fr.paulfgx.bdmobileproject.ui.viewmodel.Event
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
        viewModel.tasksRef.keepSynced(true)
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
        viewModel.getDataFromFirebase { position, event ->
            when (event) {
                Event.FIRST_LOADING -> {
                    progress_bar.visibility = View.GONE
                    toDoListAdapter.submitList(viewModel.taskList)
                }
                Event.INSERT -> {
                    toDoListAdapter.notifyItemInserted(position)
                }
                Event.UPDATE -> {
                    toDoListAdapter.notifyItemChanged(position)
                }
                Event.DELETE -> {
                    toDoListAdapter.notifyDataSetChanged()
                }
            }
        }
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
                viewModel.currentSearch = newText
                var list = viewModel.completeTaskList.toMutableList()
                viewModel.searchWith(list, newText) {
                    viewModel.taskList = it.toMutableList()
                    viewModel.updateMapWithNewPositions()
                    toDoListAdapter.submitList(viewModel.taskList)
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
            viewModel.currentSearch = ""
            fragment_main_layout.hideKeyboard()
            viewModel.taskList = viewModel.completeTaskList.toMutableList()
            toDoListAdapter.submitList(viewModel.taskList)
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
            viewModel.taskList[oldPosition].isExpanded = false
            toDoListAdapter.notifyItemChanged(oldPosition)
        }
        viewModel.taskList[newPosition].isExpanded = true
        toDoListAdapter.notifyItemChanged(newPosition)
    }

    private fun refreshAdapter() {
        viewModel.updateMapWithNewPositions()
        toDoListAdapter.submitList(viewModel.taskList)
    }

    private fun reinitExpanded() {
        if (toDoListAdapter.expendedPosition != -1 && toDoListAdapter.expendedPosition < viewModel.taskList.size)
            viewModel.taskList[toDoListAdapter.expendedPosition].isExpanded = false
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
}
