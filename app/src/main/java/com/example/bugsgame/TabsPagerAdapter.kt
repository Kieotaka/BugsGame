package com.example.bugsgame

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bugsgame.fragments.AuthorsFragment
import com.example.bugsgame.fragments.RegistrationFragment
import com.example.bugsgame.fragments.RulesFragment
import com.example.bugsgame.fragments.SettingsFragment

class TabsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> RegistrationFragment()
        1 -> RulesFragment()
        2 -> AuthorsFragment()
        3 -> SettingsFragment()
        else -> RegistrationFragment()
    }
}