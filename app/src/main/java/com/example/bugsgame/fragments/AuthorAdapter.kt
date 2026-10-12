package com.example.bugsgame.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.bugsgame.R
import com.example.bugsgame.model.Author

class AuthorAdapter(
    context: Context,
    private val authors: List<Author>
) : ArrayAdapter<Author>(context, 0, authors) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_author, parent, false)

        val author = authors[position]
        view.findViewById<TextView>(R.id.tvName).text = author.name
        view.findViewById<ImageView>(R.id.ivPhoto).setImageResource(author.photoRes)

        return view
    }
}