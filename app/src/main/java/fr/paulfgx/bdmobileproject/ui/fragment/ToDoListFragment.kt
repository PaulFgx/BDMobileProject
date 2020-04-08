package fr.paulfgx.bdmobileproject.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
        todoList.add(Task("Faire les courses", false))
        todoList.add(Task("Préparer la tartiflette", false))
        todoList.add(Task("Faire le tour du monde", true))
        todoList.add(Task("Dire bonjour à son voisin", false))
        todoList.add(Task("Avancer le projet", true))
        toDoListAdapter.submitList(todoList)
    }
}
