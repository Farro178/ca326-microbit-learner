package com.example.microbitlearner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_mc_question.*
import kotlin.random.Random

class McQuestionFragment : Fragment() {

    private val args: McQuestionFragmentArgs by navArgs()

    private lateinit var mcQuestionText: TextView
    private lateinit var mcqNoIndicator: TextView

    private lateinit var mcqRadioGroup: RadioGroup
    private lateinit var mcqFirstOption: RadioButton
    private lateinit var mcqSecondOption: RadioButton
    private lateinit var mcqThirdOption: RadioButton
    private lateinit var mcqForthOption: RadioButton

    private lateinit var mcqSubmitButton: Button
    private lateinit var mcqHintButton: Button

    private lateinit var referSnippets: DatabaseReference

    private lateinit var mcqLoadingImageView: ImageView
    private lateinit var mScroll: ScrollView


    private var snippetRangeLower = 0;
    private var snippetRangeUpper = 0
    private var mcOptionsErrors: MutableList<String> = mutableListOf()
    private var mcqCurrentQIndex: Int = 1
    private var mcqCorrectQs: Int = 0
    private var mcqtotalQs: Int = 5


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // defines what questions are part of each difficulty, these would also be stored in the DB in the future.
        when (args.difficulty) {
            "Beginner" -> {
                snippetRangeLower = 0
                snippetRangeUpper = 3
            }
            "Hard" -> {
                snippetRangeLower = 4
                snippetRangeUpper = 8
            }
        }

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

        codemcQuestion() // starts the first question
    }

    private fun codemcQuestion() {
        val randomSnippetIndex = Random.nextInt(snippetRangeLower, snippetRangeUpper)
        referSnippets = FirebaseDatabase.getInstance().reference.child("CodeRandomError") //
            .child(randomSnippetIndex.toString()) // retrieves the code snippets from the database
        referSnippets.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, "Broken!", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // in the future, this would be put in the database.
                mcOptionsErrors = arrayListOf(
                    "IndexError",
                    "SyntaxError",
                    "ImportError",
                    "TypeError",
                    "ValueError",
                    "NameError",
                    "IndentationError",
                    "No Error"
                )
                val data = dataSnapshot.value as HashMap<String, Any> // retrieves the string value (the code snippet) from the hashmap
                val snippet = data["snippet"].toString()

                val mcqCreatedError = createError(snippet) // creates an error in the code snippet, as well as the answer and the hint to that question.
                val mcqAnswer = mcqCreatedError[0]
                val mcqError = mcqCreatedError[1]
                mcQuestionText.text =
                    "What error will this code snippet output if there is one?\n\n$mcqError" // put a question in front of every code snippet
                val mcqhint = mcqCreatedError[2]
                val optionList: ArrayList<String> =
                    randomAnswerDB(mcOptionsErrors as ArrayList<String>, mcqAnswer)
                mcqNoIndicator.text = "$mcqCurrentQIndex / $mcqtotalQs"
                mcqFirstOption.text = optionList[0]
                mcqSecondOption.text = optionList[1]
                mcqThirdOption.text = optionList[2]
                mcqForthOption.text = optionList[3]
                mcqLoadingImageView.visibility = View.GONE // hides the loading image
                mScroll.visibility = View.VISIBLE // sets the scroll view to visible once options have loaded

                val isRadioSelected = mcqRadioGroup.checkedRadioButtonId // stores whether a radio button has been selected or not
                mcqHintButton.setOnClickListener {
                    Toast.makeText(context, mcqhint, Toast.LENGTH_SHORT).show()
                }

                if (isRadioSelected != -1) { // if a radio button has been selected
                    mcqSubmitButton.setOnClickListener { // the submit button can now be pressed
                        // if selected answer is correct

                        if ((mcqFirstOption.isChecked && mcqFirstOption.text == mcqAnswer) || (mcqSecondOption.isChecked && mcqSecondOption.text == mcqAnswer) || (mcqThirdOption.isChecked && mcqThirdOption.text == mcqAnswer) || (mcqForthOption.isChecked && mcqForthOption.text == mcqAnswer)){

                            if (mcqCurrentQIndex < mcqtotalQs) { // checks to see if it is the final question
                                Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
                                mcqCorrectQs += 1
                                mcqCurrentQIndex += 1
                                codemcQuestion()

                            } else { // if it is the final answer, it moves on the the results page
                                Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show()
                                mcqCorrectQs += 1
                                val action =
                                    McQuestionFragmentDirections.actionMcQuestionFragmentToQuizResultFragment(
                                        mcqCorrectQs,
                                        mcqtotalQs
                                    )
                                findNavController().navigate(action)
                            }

                        } else { // if the selected answer is wrong
                            if (mcqCurrentQIndex < mcqtotalQs) {
                                Toast.makeText(context, "Wrong...", Toast.LENGTH_SHORT).show()
                                mcqCurrentQIndex += 1
                                codemcQuestion()

                            } else {
                                Toast.makeText(context, "Wrong...", Toast.LENGTH_SHORT).show()
                                val action =
                                    McQuestionFragmentDirections.actionMcQuestionFragmentToQuizResultFragment(
                                        mcqCorrectQs,
                                        mcqtotalQs
                                    )
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
        options.shuffle()  // shuffles the arraylist
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
        options.shuffle() // shuffles the array list once more
        return options // returns the list of options

    }

    // function that may remove a colon if one is present
    private fun takeAwayColon(lines: ArrayList<String>): ArrayList<String> {
        var i = 0
        while (i <= lines.size - 1) { //checks every line of code

            val insideRandomised = Random.nextInt(0, 3) // a randomised number so that the first occurrence of the searched for value is not always the one returned
            if ((lines[i].contains("while", ignoreCase = false) || lines[i].contains(
                    "if",
                    ignoreCase = false
                ) || lines[i].contains("else", ignoreCase = false) || lines[i].contains(
                    "elif",
                    ignoreCase = false
                ) || lines[i].contains("for", ignoreCase = false) || lines[i].contains(
                    "def",
                    ignoreCase = false
                )) && insideRandomised != 0 // if the randomised number is 0, it will skip to the next line.
            ) {
                lines[i] = lines[i].replace(":", "") // replaces the colon with an empty string
                i += 1
                return lines // once a line has changed, the snippet is returned

            } else { // if a colon was not found and the randomised number was equal to 0
                i += 1
            }
        }
        return lines
    }

    // function that may remove parentheses from the code snippet if one is present
    private fun takeAwayParentheses(lines: ArrayList<String>): ArrayList<String> {
        var i = 0
        while (i < lines.size - 1) {

            val insideRandomised = Random.nextInt(0, 3) // a randomised number so that the first occurrence of the searched for value is not always the one returned
            if ((lines[i].contains("(") && lines[i].contains(")")) && insideRandomised != 0) {
                lines[i] = lines[i].replace("(", " ")
                lines[i] = lines[i].replace(")", "")
                i += 1
                return lines // if a parentheses occurs and the randomised number is not 0, the snippet is returned

            } else {
                i += 1
            }
        }
        return lines // if a parentheses does not occur, it returns the snippet
    }

    // function that may remove indentations from the code snippet if one is present
    private fun takeAwayIndentation(lines: ArrayList<String>): ArrayList<String> {
        var i = 0
        val newLine: MutableList<String>
        while (i <= lines.size - 1) {

            val insideRandomised = Random.nextInt(0, 3) // a randomised number so that the first occurrence of the searched for value is not always the one returned
            if ((lines[i].contains("while", ignoreCase = false) || lines[i].contains(
                    "if",
                    ignoreCase = false
                ) || lines[i].contains("else", ignoreCase = false) || lines[i].contains(
                    "elif",
                    ignoreCase = false
                ) || lines[i].contains("for", ignoreCase = false) || lines[i].contains(
                    "def",
                    ignoreCase = false
                )) && insideRandomised != 0
            ) {
                newLine = lines[i + 1].split(" ").toMutableList() // splits the next string at any space
                lines[i + 1] = lines[i + 1].replace(lines[i + 1], newLine.joinToString("")) // joins the string again replacing the old one with a string that has no indentation
                i += 1
                return lines // if changed, the code snippet is returned

            } else {
                i += 1
            }
        }
        return lines
    }

    // this function decides what error is produced and manages the creation of errors in the code snippet
    private fun createError(snippet: String): ArrayList<String> {
        val lines: MutableList<String> = snippet.split("\n").toMutableList()
        var mcqAnswer = ""
        var hint = ""
        val takenAwayQuestion = ""
        when (Random.nextInt(0, 3)) { // randomises what error will occur

            0 -> { // This is for taking the ":" away from a while, if, for, elif or else statement
                val takenAway: ArrayList<String> = takeAwayColon(lines as ArrayList<String>) // passed the snippet to the take away colon function
                val takenAwayQuestion = takenAway.joinToString("\n")
                if (takenAwayQuestion != snippet) { // if an error has been introduced to the snippet
                    mcqAnswer = "SyntaxError"
                    hint = "Look for missing colons"

                } else {
                    mcqAnswer = "No Error"
                    hint = "There might not be an error"


                }
                return arrayListOf(mcqAnswer, takenAwayQuestion, hint)
            }

            1 -> { // This is for taking parentheses away from a snippet
                val takenAway: ArrayList<String> = takeAwayParentheses(lines as ArrayList<String>)
                val takenAwayQuestion = takenAway.joinToString("\n")
                if (takenAwayQuestion != snippet) { // if an error has been introduced to the snippet
                    mcqAnswer = "SyntaxError"
                    hint = "Look at Parentheses!"

                } else {
                    mcqAnswer = "No Error"
                    hint = "There might not be an error!"
                }
                return arrayListOf(mcqAnswer, takenAwayQuestion, hint)
            }
            2 -> { // This is for taking away indentation from a snippet
                val takenAway: MutableList<String> = takeAwayIndentation(lines as ArrayList<String>)
                val takenAwayQuestion = takenAway.joinToString("\n")
                if (takenAwayQuestion != snippet) {
                    mcqAnswer = "IndentationError"
                    hint = "Look at indentation!"
                } else {
                    mcqAnswer = "No Error"
                    hint = "There might not be an error!"
                }
                return arrayListOf(mcqAnswer, takenAwayQuestion, hint)
            }
        }
        return arrayListOf(mcqAnswer, takenAwayQuestion, hint) //if an error has occurred
    }
}
