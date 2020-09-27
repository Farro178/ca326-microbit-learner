package com.example.microbitlearner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.*
import kotlin.random.Random


class McqPremadeFragment : Fragment() {

    private lateinit var mcqRadioGroup: RadioGroup
    private lateinit var mcqFirstOption: RadioButton
    private lateinit var mcqSecondOption: RadioButton
    private lateinit var mcqThirdOption: RadioButton
    private lateinit var mcqForthOption: RadioButton

    private lateinit var mcqHintButton: Button
    private lateinit var mcqSubmitButton: Button

    private lateinit var mcqNoIndicator: TextView
    private lateinit var mcQuestionText: TextView

    private lateinit var referPremade: DatabaseReference

    private lateinit var mcqLoadingImageView: ImageView
    private lateinit var mScroll: ScrollView


    var mcqCurrentQIndex: Int = 1
    var mcqCorrectQs: Int = 0
    var mcqtotalQs: Int = 3

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_mc_question, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mcQuestionText = view.findViewById(R.id.questionText)
        mcqRadioGroup = view.findViewById(R.id.questionRadioGroup)
        mcqFirstOption = view.findViewById(R.id.firstOptionRadioButton)
        mcqSecondOption = view.findViewById(R.id.secondOptionRadioButton)
        mcqThirdOption = view.findViewById(R.id.thirdOptionRadioButton)
        mcqForthOption = view.findViewById(R.id.forthOptionRadioButton)
        mcqSubmitButton = view.findViewById(R.id.submitButton)
        mcqNoIndicator = view.findViewById(R.id.no_indicator)
        mcqHintButton = view.findViewById(R.id.mcqhintButton)
        mcqLoadingImageView = view.findViewById<ImageView>(R.id.mcqloadingImage)
        mScroll = view.findViewById<ScrollView>(R.id.mcqSV)

        updatemcQuestion() // load first question

    }


    private fun updatemcQuestion() {

        val randomSnippetIndex = Random.nextInt(0, 3)
        referPremade = FirebaseDatabase.getInstance().reference.child("Questions")
            .child(randomSnippetIndex.toString()) // references each question in the database
        referPremade.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, "Broken!", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var data = dataSnapshot.value as HashMap<String, Any> // gets all the options for the question from the DB
                val optionuList: MutableList<String> = mutableListOf(
                    data["option1"].toString(),
                    data["option2"].toString(),
                    data["option3"].toString(),
                    data["option4"].toString(),
                    data["option5"].toString(),
                    data["option6"].toString(),
                    data["option7"].toString(),
                    data["option8"].toString()
                )

                val optionList =
                    randomAnswerDB(optionuList as ArrayList<String>, data["answer"].toString()) //puts all options and answer in an array list
                mcQuestionText.text = data["question"].toString() // sets the question text box to the question
                mcqNoIndicator.text = "$mcqCurrentQIndex / $mcqtotalQs" // keeps track of current question
                mcqFirstOption.text = optionList[0]
                mcqSecondOption.text = optionList[1]
                mcqThirdOption.text = optionList[2]
                mcqForthOption.text = optionList[3]
                mcqLoadingImageView.visibility = View.GONE //// hides the loading image once database has loaded
                mScroll.visibility = View.VISIBLE //sets the scroll view to visible once options have loaded

                mcqHintButton.setOnClickListener {
                    Toast.makeText(context, data["hint"].toString(), Toast.LENGTH_SHORT).show() //displays hint when button is pressed
                }

                val isRadioSelected = mcqRadioGroup.checkedRadioButtonId
                if (isRadioSelected != -1) { // if radiobutton is selected
                    mcqSubmitButton.setOnClickListener { // submit button will start working

                        // if correct answer is submitted
                        if ((mcqFirstOption.isChecked && mcqFirstOption.text == data["answer"].toString()) || (mcqSecondOption.isChecked && mcqSecondOption.text == data["answer"].toString()) || (mcqThirdOption.isChecked && mcqThirdOption.text == data["answer"].toString()) || (mcqForthOption.isChecked && mcqForthOption.text == data["answer"].toString())) {

                            if (mcqCurrentQIndex < mcqtotalQs) {
                                Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
                                mcqCorrectQs += 1
                                mcqCurrentQIndex += 1
                                updatemcQuestion()

                            } else { // if the current question is the last question
                                Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
                                mcqCorrectQs += 1
                                val action = McqPremadeFragmentDirections.actionMcqPremadeFragmentToQuizResultFragment(mcqCorrectQs, mcqtotalQs)
                                findNavController().navigate(action) // navigate to results
                            }

                        } else { // if the wrong answer is submitted

                            if (mcqCurrentQIndex < mcqtotalQs) {
                                Toast.makeText(context, "Wrong...", Toast.LENGTH_SHORT).show()
                                mcqCurrentQIndex += 1
                                updatemcQuestion()

                            } else {
                                Toast.makeText(context, "Wrong...", Toast.LENGTH_SHORT).show()
                                mcqCurrentQIndex += 1
                                val action = McqPremadeFragmentDirections.actionMcqPremadeFragmentToQuizResultFragment(mcqCorrectQs, mcqtotalQs)
                                findNavController().navigate(action)
                            }
                        }
                    }
                }
            }
        })
    }
    // function used to randomise the options for each answer but maintains the correct answer, so in the unlikely chance the same code fragment is asked twice, the options should not be the same
    private fun randomAnswerDB(options: ArrayList<String>, rAnswer: String): ArrayList<String> {
        options.shuffle() //shuffles the options
        var deleted = 0 // variable to count how many options have been removed
        var i = 0
        val iterateOptions: MutableListIterator<String> = options.listIterator()
        while (deleted <= 3 && iterateOptions.hasNext()) {

            if (options[i] != rAnswer) { // removes option if its not the answer
                options.remove(options[i])
                deleted++
                i++

            } else if (options[i] == rAnswer) {
                i++
            }
        }
        options.shuffle()
        return options// returns the list of options

    }
}
