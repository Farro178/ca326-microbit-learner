package com.example.microbitlearner

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

// in the future - this fragment could be used for "re-type this line" debugging questions
class QuizFragment : Fragment() {

    private val args: QuizFragmentArgs by navArgs()

    private lateinit var mQuestionText: TextView
    private lateinit var mHintText: TextView

    private lateinit var mAnswer: EditText
    private lateinit var mAnswerText: EditText
    private lateinit var mAnswerNumber: EditText

    private lateinit var mSubmit: Button
    private lateinit var mDone: Button
    private lateinit var mHint: Button

    private var mCurrentQIndex = 0
    private var mCorrectQs = 0

    private var allQuestions = listOf(
        listOf(
            TypeQuestion(
                R.string.topic1_q1,
                R.string.topic1_q1_ans,
                R.string.topic1_q1_hint,
                R.string.text
            ),
            TypeQuestion(
                R.string.topic1_q2,
                R.string.topic1_q2_ans,
                R.string.topic1_q2_hint,
                R.string.digit
            ),
            TypeQuestion(
                R.string.topic1_q3,
                R.string.topic1_q3_ans,
                R.string.topic1_q3_hint,
                R.string.text
            ),
            TypeQuestion(
                R.string.topic1_q4,
                R.string.topic1_q4_ans,
                R.string.topic1_q4_hint,
                R.string.digit
            )
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var topicQuestionsIndex: Int = args.topic
        var mQuestions: List<TypeQuestion> =
            allQuestions[topicQuestionsIndex] // get list of questions for this topic

        mQuestionText = view.findViewById<TextView>(R.id.questionTv)
        mHintText = view.findViewById<TextView>(R.id.hintTv)
        mAnswerText = view.findViewById<EditText>(R.id.answerTextEdt)
        mAnswerNumber = view.findViewById<EditText>(R.id.answerNumberEdt)
        mSubmit = view.findViewById<Button>(R.id.subButton)
        mDone = view.findViewById<Button>(R.id.doneBtn)
        mHint = view.findViewById<Button>(R.id.hintBtn)

        setEditText(mQuestions[mCurrentQIndex].getInputMthd())
        changeQuestion(
            mQuestions[mCurrentQIndex].getQuestion(),
            mQuestions[mCurrentQIndex].getHint()
        )
        setBarTitle(mCurrentQIndex, mQuestions.size)

        mSubmit.setOnClickListener {
            val userInputAnswer = mAnswer.text.toString() // retrieve user input
            mAnswer.text.clear() // clear previous answer

            if (mCurrentQIndex < mQuestions.size) {
                correctAnswer(userInputAnswer, getString(mQuestions[mCurrentQIndex].getAnswer()))
                mCurrentQIndex = (mCurrentQIndex + 1)
                changeQuestion(
                    mQuestions[mCurrentQIndex].getQuestion(),
                    mQuestions[mCurrentQIndex].getHint()
                )
                setBarTitle(mCurrentQIndex, mQuestions.size)
            }

            // check if final question has been reached
            if (mCurrentQIndex + 1 == mQuestions.size) {
                mSubmit.visibility = View.GONE
                mDone.visibility = View.VISIBLE
            }

            // reset hint button and text
            mHint.visibility = View.VISIBLE
            mHintText.visibility = View.GONE

            // find the input method for the next question (number, text)
            setEditText(mQuestions[mCurrentQIndex].getInputMthd())
        }

        // final answer submitted
        mDone.setOnClickListener {
            val userInputAnswer = mAnswer.text.toString()
            correctAnswer(userInputAnswer, getString(mQuestions[mCurrentQIndex].getAnswer()))
            val action = QuizFragmentDirections.actionQuizFragmentToQuizResultFragment(
                mCorrectQs,
                mQuestions.size
            )
            findNavController().navigate(action) // go to results screen
        }

        mHint.setOnClickListener { // display hint
            mHint.visibility = View.GONE
            mHintText.visibility = View.VISIBLE
        }
    }

    private fun correctAnswer(userAnswer: String, correctAnswer: String): Boolean { // check user has entered the correct answer
        mAnswer.hideKeyboard() // remove keyboard so it doesn't remain for next question or the results
        return if (userAnswer.toLowerCase() == correctAnswer) { // correct answer
            mCorrectQs += 1
            Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show() // instant feedback
            true
        } else { // incorrect answer
            Toast.makeText(context, "Wrong...", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun View.hideKeyboard() { // remove keyboard from screen
        val inpMthdMgr =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inpMthdMgr.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setEditText(inputMthd: Int) { // change keyboard type between digit and text
        if (inputMthd == R.string.text) {
            mAnswer = mAnswerText
            mAnswerText.visibility = View.VISIBLE
            mAnswerNumber.visibility = View.GONE
        } else { // input method is number
            mAnswer = mAnswerNumber
            mAnswerNumber.visibility = View.VISIBLE
            mAnswerText.visibility = View.GONE
        }
    }

    private fun changeQuestion(questionString: Int, hintString: Int) { // update question and hint textviews
        mQuestionText.text = getString(questionString)
        mHintText.text = getString(hintString)
    }

    private fun setBarTitle(numCurrentQ: Int, numQs: Int) { // change the bar title to display the current question number
        (activity as AppCompatActivity).supportActionBar?.title =
            getString(R.string.question_title, numCurrentQ + 1, numQs)
    }
}