package com.example.bugsgame

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bugsgame.fragments.*

class TabsPagerAdapter(activity: FragmentActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> RegistrationFragment()
        1 -> RulesFragment()
        2 -> AuthorsFragment()
        3 -> SettingsFragment()
        4 -> GameFragment()
        else -> RegistrationFragment()
    }
}