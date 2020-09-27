package com.example.microbitlearner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.example.microbitlearner.databinding.FragmentTopicsBinding

// menu for selecting an activity
class TopicsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentTopicsBinding = DataBindingUtil.inflate<FragmentTopicsBinding>(
            inflater,
            R.layout.fragment_topics,
            container,
            false
        )

        // navigate to "Typing questions quiz"
        binding.topic1Button.setOnClickListener {
            val action = TopicsFragmentDirections.actionTopicsFragmentToQuizFragment(0)
            findNavController().navigate(action)
        }

        // navigate to "Multiple Choice Quiz"
        binding.topic2Button.setOnClickListener {
            val action = TopicsFragmentDirections.actionTopicsFragmentToMcqPremadeFragment()
            findNavController().navigate(action)
        }

        // navigate to "Code Snippets Quiz: Easy"
        binding.topic3Button.setOnClickListener {
            val action =
                TopicsFragmentDirections.actionTopicsFragmentToRandomisedCodeFragment("Beginner")
            findNavController().navigate(action)
        }

        // navigate to "Code Snippets Quiz: Medium"
        binding.topic4Button.setOnClickListener {
            val action =
                TopicsFragmentDirections.actionTopicsFragmentToRandomisedCodeFragment("Medium")
            findNavController().navigate(action)
        }

        // navigate to "Code Snippets Quiz: Hard"
        binding.topic5Button.setOnClickListener {
            val action =
                TopicsFragmentDirections.actionTopicsFragmentToRandomisedCodeFragment("Hard")
            findNavController().navigate(action)
        }

        // navigate to "Code Errors Quiz: Easy"
        binding.topic6Button.setOnClickListener {
            val action =
                TopicsFragmentDirections.actionTopicsFragmentToMcQuestionFragment("Beginner")
            findNavController().navigate(action)
        }

        // navigate to "Code Errors Quiz: Hard"
        binding.topic7Button.setOnClickListener {
            val action = TopicsFragmentDirections.actionTopicsFragmentToMcQuestionFragment("Hard")
            findNavController().navigate(action)
        }

        return binding.root
    }
}