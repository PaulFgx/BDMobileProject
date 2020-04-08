package fr.paulfgx.bdmobileproject.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import fr.paulfgx.bdmobileproject.R
import fr.paulfgx.bdmobileproject.data.model.ToDoItem
import fr.paulfgx.bdmobileproject.ui.activity.MainActivity
import fr.paulfgx.bdmobileproject.ui.adapter.ToDoListAdapter
import kotlinx.android.synthetic.main.fragment_todolist.view.*

class ToDoListFragment : Fragment() {

    private lateinit var toDoListAdapter: ToDoListAdapter

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
        toDoListAdapter = ToDoListAdapter()
        view.todo_list_recycler_view.apply {
            adapter = toDoListAdapter
            if (itemDecorationCount == 0) addItemDecoration(ToDoListAdapter.OffsetDecoration())
        }

        loadAdapter()
    }

    private fun loadAdapter()
    {
        var todoList = mutableListOf<ToDoItem>()
        todoList.add(ToDoItem("Faire les courses", false))
        todoList.add(ToDoItem("Préparer la tartiflette", false))
        todoList.add(ToDoItem("Faire le tour du monde en pédalo", true))
        todoList.add(ToDoItem("Dire bonjour à son voisin", false))
        todoList.add(ToDoItem("Avancer le projet", true))
        toDoListAdapter.submitList(todoList)
    }
}
