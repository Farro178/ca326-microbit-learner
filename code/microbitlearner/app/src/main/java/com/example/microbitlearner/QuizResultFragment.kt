package com.example.microbitlearner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.navArgs

class QuizResultFragment : Fragment() {

    private val args: QuizResultFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quizresult, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val resultTv = view.findViewById<TextView>(R.id.resultDisplayTv)

        val numCorrectQs = args.numCorrectQuestions // user score
        val numQs = args.numQuestions // total questions asked

        val result: Int = ((numCorrectQs.toDouble() / numQs) * 100).toInt() // find result as percentage

        val message = when {
            result < 50 -> {
                "You should revise the material covered in this quiz."
            }
            result in 50..84 -> {
                "Keep up the hard work to improve your score even more!"
            }
            else -> {
                "You know your stuff! It is clear you are learning the material. Well done."
            }
        }

        val resultTvText: String =
            "You got $numCorrectQs correct out of $numQs questions. This gives you a result of $result%. $message"
        resultTv.text = resultTvText // display quiz result
    }
}
