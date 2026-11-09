package com.example.bugsgame.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bugsgame.R
import com.example.bugsgame.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecordsAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_records, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        val btnClear = view.findViewById<Button>(R.id.btnClearRecords)

        database = AppDatabase.getDatabase(requireContext())
        adapter = RecordsAdapter()

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Подписка на обновления из БД
        viewLifecycleOwner.lifecycleScope.launch {
            database.scoreDao().getAllScores().collectLatest { scores ->
                adapter.setData(scores)
            }
        }

        // Обработчик кнопки очистки
        btnClear.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Очистить рекорды?")
                .setMessage("Все сохраненные результаты будут удалены. Вы уверены?")
                .setPositiveButton("Да") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        database.scoreDao().clearAllScores()
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        return view
    }
}