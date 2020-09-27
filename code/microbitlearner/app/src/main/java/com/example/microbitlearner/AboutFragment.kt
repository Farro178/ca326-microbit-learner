package com.example.microbitlearner

import android.view.View
import android.view.ViewGroup
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater

class AboutFragment : Fragment() { // displays info about the app

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }
}