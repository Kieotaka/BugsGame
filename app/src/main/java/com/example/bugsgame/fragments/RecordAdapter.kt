package com.example.bugsgame.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bugsgame.R
import com.example.bugsgame.data.Score

class RecordsAdapter : RecyclerView.Adapter<RecordsAdapter.ViewHolder>() {
    private var scores: List<Score> = emptyList()

    fun setData(newScores: List<Score>) {
        scores = newScores
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val score = scores[position]
        holder.tvName.text = "${position + 1}. ${score.playerName}"
        holder.tvPoints.text = "${score.points} очков"
        holder.tvDifficulty.text = "Сложность: ${score.difficulty} | Точность: ${score.accuracy}%"
    }

    override fun getItemCount() = scores.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvPoints: TextView = view.findViewById(R.id.tvPoints)
        val tvDifficulty: TextView = view.findViewById(R.id.tvDifficulty)
    }
}