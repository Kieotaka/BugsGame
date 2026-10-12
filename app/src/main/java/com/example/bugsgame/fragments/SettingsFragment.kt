package com.example.bugsgame.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bugsgame.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireContext().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загрузка сохранённых значений
        binding.sbSpeed.progress = prefs.getInt("speed", 5)
        binding.sbMaxBugs.progress = prefs.getInt("max_bugs", 10)
        binding.sbBonusInterval.progress = prefs.getInt("bonus_interval", 15)
        binding.sbRoundDuration.progress = prefs.getInt("round_duration", 60)

        binding.btnSave.setOnClickListener {
            prefs.edit()
                .putInt("speed", binding.sbSpeed.progress)
                .putInt("max_bugs", binding.sbMaxBugs.progress)
                .putInt("bonus_interval", binding.sbBonusInterval.progress)
                .putInt("round_duration", binding.sbRoundDuration.progress)
                .apply()

            Toast.makeText(requireContext(), "Сохранено", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}