package com.example.bugsgame.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bugsgame.R
import com.example.bugsgame.model.Author

class AuthorsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_authors, container, false)
        val listView = view.findViewById<ListView>(R.id.listView)

        val authors = listOf(
            Author("Белоусов Михаил Максимович", R.drawable.autor)
        )

        listView.adapter = AuthorAdapter(requireContext(), authors)

        listView.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(requireContext(),
                authors[position].name, Toast.LENGTH_SHORT).show()
        }

        return view
    }
}