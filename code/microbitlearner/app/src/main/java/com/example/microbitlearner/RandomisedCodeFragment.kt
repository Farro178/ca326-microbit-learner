package com.example.microbitlearner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.database.*
import kotlin.math.round
import kotlin.random.Random

open class RandomisedCodeFragment : Fragment() {

    private val args: RandomisedCodeFragmentArgs by navArgs()

    private lateinit var mQuestionTxt: TextView
    private lateinit var mCodeTxt: TextView

    private lateinit var mAnswerBtn1: Button
    private lateinit var mAnswerBtn2: Button
    private lateinit var mAnswerBtn3: Button
    private lateinit var mAnswerBtn4: Button

    private lateinit var mLoadingImageView: ImageView

    private lateinit var referSnippet: DatabaseReference
    private lateinit var referImages: DatabaseReference
    private lateinit var referStrings: DatabaseReference

    var mOptionsImages: MutableList<String> = mutableListOf()
    var mOptionsStrings: MutableList<String> = mutableListOf()
    var answerOptions: MutableList<String> = mutableListOf()

    private var mCorrectQs = 0
    private var mNumQsCompleted = 0
    private var mNumQsTotal = 0
    private var snippetRangeLower = 0
    private var snippetRangeUpper = 0

    var correctAnswer = ""

    // regular expressions used to find missing arguments within Python code snippets
    private val regShowImage = """(.*)display.show\(Image.\)(.*)""".toRegex()
    private val regScrollString = """(.*)display.scroll\(\)(.*)""".toRegex()
    private val regLengthString = """(.*)len\(\)(.*)""".toRegex()
    private val regRangeNums = """(.*)randint\(\)(.*)""".toRegex()
    private val regVarX = """(.*)x = """.toRegex()
    private val regVarY = """(.*)y = """.toRegex()
    private val regVarI = """(.*)i = """.toRegex()
    private val regWhileI = """while i (.*) :""".toRegex()
    private val regSleep = """(.*)sleep\(\)""".toRegex()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        referImages = FirebaseDatabase.getInstance()
            .reference.child("Images") // retrieve the list of pre-defined images that a micro:bit can display from the DB
        referImages.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, "Broken!", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var data = dataSnapshot.value as MutableList<String>
                data.shuffle() // randomise the order of the images
                mOptionsImages = data
            }
        })

        referStrings = FirebaseDatabase.getInstance()
            .reference.child("Strings") // retrieve the list of various strings from the DB
        referStrings.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, "Broken!", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var data = dataSnapshot.value as MutableList<String>
                data.shuffle() // randomise the order of the strings
                mOptionsStrings = data
            }
        })

        // in the future - could store these ranges and numbers for individual topics in the DB to be more dynamic
        when (args.difficulty) { // define which questions can be asked for each difficulty, as well as how many
            "Beginner" -> { // 0 - 4
                snippetRangeLower = 0
                snippetRangeUpper = 4
                mNumQsTotal = 5
            }
            "Medium" -> { // 3 - 10
                snippetRangeLower = 3
                snippetRangeUpper = 10
                mNumQsTotal = 10
            }
            "Hard" -> { // 3 - 19
                snippetRangeLower = 3
                snippetRangeUpper = 19
                mNumQsTotal = 15
            }
        }

        return inflater.inflate(R.layout.fragment_randomised_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        mQuestionTxt = view.findViewById<TextView>(R.id.questionTv)
        mCodeTxt = view.findViewById<TextView>(R.id.codeTv)
        mAnswerBtn1 = view.findViewById<Button>(R.id.answerBtn1)
        mAnswerBtn2 = view.findViewById<Button>(R.id.answerBtn2)
        mAnswerBtn3 = view.findViewById<Button>(R.id.answerBtn3)
        mAnswerBtn4 = view.findViewById<Button>(R.id.answerBtn4)
        mLoadingImageView = view.findViewById<ImageView>(R.id.loadingImage)

        getNewQ() // first question

        mAnswerBtn1.setOnClickListener() {
            // user chooses answer, evaluate it then proceed to next question/results
            correctAnswer(mAnswerBtn1.text.toString()) // use the text displayed on the chosen button
            getNewQ()
        }
        mAnswerBtn2.setOnClickListener() {
            correctAnswer(mAnswerBtn2.text.toString())
            getNewQ()
        }
        mAnswerBtn3.setOnClickListener() {
            correctAnswer(mAnswerBtn3.text.toString())
            getNewQ()
        }
        mAnswerBtn4.setOnClickListener() {
            correctAnswer(mAnswerBtn4.text.toString())
            getNewQ()
        }
    }

    private fun correctAnswer(buttonTxt: String) { // check if the answer chosen by the user is correct
        if (buttonTxt == correctAnswer) {
            mCorrectQs += 1 // increment user score
            Toast.makeText(context, "Correct!", Toast.LENGTH_SHORT).show() // instant feedback
        } else {
            Toast.makeText(context, "Wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNewQ() { // change question or go to results

        mOptionsImages.shuffle(); mOptionsStrings.shuffle() // shuffle image and string options for each new question

        if (mNumQsCompleted < mNumQsTotal) { // final question has not been reached

            val randomSnippetIndex = Random.nextInt(
                snippetRangeLower,
                snippetRangeUpper
            ) // choose random template number within range for difficulty
            referSnippet = FirebaseDatabase.getInstance().reference.child("CodeSnippetTemplates")
                .child(randomSnippetIndex.toString()) // retrieve the code snippet template from the DB
            referSnippet.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(context, "Broken!", Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var data = dataSnapshot.value as HashMap<String, Any>

                    mQuestionTxt.text = data["question"].toString() // display question text

                    answerOptions = mutableListOf() // remove any answers from previous questions

                    mCodeTxt.text = insertArgs(
                        data["snippet"].toString(),
                        randomSnippetIndex
                    ) // display code text with template "filled in"

                    mLoadingImageView.visibility =
                        View.GONE // hide loading image once data is retrieved and ready to display

                    setBarTitle(mNumQsCompleted, mNumQsTotal) // change question number
                    mQuestionTxt.visibility =
                        View.VISIBLE // display all other components - only really makes a change for the first question
                    mCodeTxt.visibility = View.VISIBLE
                    mAnswerBtn1.visibility = View.VISIBLE
                    mAnswerBtn2.visibility = View.VISIBLE
                    mAnswerBtn3.visibility = View.VISIBLE
                    mAnswerBtn4.visibility = View.VISIBLE

                    answerOptions.shuffle() // display answers in a random order
                    mAnswerBtn1.text = answerOptions[0] // display answers options in button text
                    mAnswerBtn2.text = answerOptions[1]
                    mAnswerBtn3.text = answerOptions[2]
                    mAnswerBtn4.text = answerOptions[3]
                    mNumQsCompleted += 1
                }
            })
        } else { // quiz completed
            val action =
                RandomisedCodeFragmentDirections.actionRandomisedCodeFragmentToQuizResultFragment(
                    mCorrectQs,
                    mNumQsTotal
                )
            findNavController().navigate(action) // navigate to the results screen
        }
    }

    fun insertArgs(snippet: String, snippetNum: Int): String {

        var argsInserted: MutableList<String> =
            mutableListOf() // list of strings with arguments inserted where necessary
        var stringSplit: MutableList<String>

        var imagesUsed: Int = 0
        var stringsUsed: Int = 0
        var intsUsed: Int = 0
        var intI = 0
        var whileInt = 0

        var lines: MutableList<String> =
            snippet.split("\n").toMutableList() // individual lines in the code snippet template
        for (line in lines) {
            when {
                regShowImage.matches(line) -> { // line needs image argument
                    stringSplit = (line.split(".")).toMutableList()
                    stringSplit[stringSplit.indexOf(")")] =
                        "${mOptionsImages[imagesUsed]})" // insert argument

                    answerOptions.add(mOptionsImages[imagesUsed]); answerOptions.add(mOptionsImages[imagesUsed + 2])
                    imagesUsed += 1

                    correctAnswer = if (snippetNum == 10) {
                        mOptionsImages[1] // correct answer is second image
                    } else {
                        mOptionsImages[0] // correct answer is first image
                    }

                    if (snippetNum == 1 || snippetNum == 5) { // this regex only matches once for these snippets so 2 more answers are needed
                        answerOptions.add(mOptionsImages[imagesUsed]); answerOptions.add(
                            mOptionsImages[imagesUsed + 2]
                        )
                    }

                    argsInserted.add(stringSplit.joinToString(".")) // add the resulting string
                }
                regScrollString.matches(line) -> { // line needs string argument
                    stringSplit = line.split(".").toMutableList()

                    correctAnswer = mOptionsStrings[stringsUsed]
                    stringSplit[stringSplit.indexOf("scroll()")] = "scroll(${correctAnswer})"
                    correctAnswer = removeQuotation(correctAnswer)

                    answerOptions.add(correctAnswer); answerOptions.add(
                        removeQuotation(
                            mOptionsStrings[stringsUsed + 1]
                        )
                    ); answerOptions.add(removeQuotation(mOptionsStrings[stringsUsed + 2])); answerOptions.add(
                        removeQuotation(mOptionsStrings[stringsUsed + 3])
                    ) // add 4 different images to the answer options - incl. correct answer

                    argsInserted.add(stringSplit.joinToString("."))
                }
                regLengthString.matches(line) -> { // line needs string argument
                    stringSplit = line.split(".").toMutableList()

                    var correctAnswerInt =
                        (removeQuotation(mOptionsStrings[stringsUsed])).length // answer to question is the length of the string
                    correctAnswer = correctAnswerInt.toString()

                    stringSplit[stringSplit.indexOf("show(str(len()))")] =
                        "show(str(len(${mOptionsStrings[stringsUsed]})))"

                    answerOptions.add(correctAnswer); answerOptions.add((correctAnswerInt - 1).toString()); answerOptions.add(
                        (correctAnswerInt + 1).toString()
                    ); answerOptions.add((correctAnswerInt + 2).toString()) // add 4 answer options - 1 correct and 3 close numbers

                    argsInserted.add(stringSplit.joinToString("."))
                }
                regRangeNums.matches(line) -> { // line needs 2 integer arguments
                    stringSplit = line.split(".").toMutableList()

                    // 2 random numbers and 2 close numbers
                    var randomNum1 = Random.nextInt(-10, 10)
                    var randomNum1Plus1 = randomNum1 + 1
                    var randomNum2 = Random.nextInt(randomNum1 + 1, 20)
                    var randomNum2Minus1 = randomNum2 - 1

                    correctAnswer = "$randomNum1 to $randomNum2"
                    stringSplit[stringSplit.indexOf("randint()))")] =
                        "randint($randomNum1, $randomNum2)))"

                    answerOptions.add(correctAnswer); answerOptions.add("$randomNum1Plus1 to $randomNum2"); answerOptions.add(
                        "$randomNum1 to $randomNum2Minus1"
                    ); answerOptions.add("$randomNum1Plus1 to $randomNum2Minus1") // add 4 answer options - 1 correct and 3 with similar ranges

                    argsInserted.add(stringSplit.joinToString("."))
                }
                regVarX.matches(line) -> { // line needs string value for variable assignment
                    stringSplit = line.split("=").toMutableList()

                    correctAnswer = removeQuotation(mOptionsStrings[0])
                    stringSplit[stringSplit.indexOf(" ")] = " \"${correctAnswer}\""


                    answerOptions.add(0, correctAnswer); answerOptions.add(
                        1,
                        removeQuotation(mOptionsStrings[1])
                    ); answerOptions.add(2, removeQuotation(mOptionsStrings[2])); answerOptions.add(
                        3,
                        removeQuotation(mOptionsStrings[3])
                    ) // add 4 answer options - 1 correct and 3 other strings

                    argsInserted.add(stringSplit.joinToString("="))
                }
                regVarY.matches(line) -> { // line needs integer value for variable assignment
                    var randomNumInt = Random.nextInt(-100, 100)

                    if (intsUsed == 0) { // first number is being inserted
                        if (snippetNum == 12) { // correct answer is first used
                            correctAnswer = randomNumInt.toString()
                        }
                        answerOptions.add(randomNumInt.toString())
                    } else if (intsUsed == 1) { // second number is being inserted
                        if (snippetNum == 11) { // correct answer is second used
                            correctAnswer = randomNumInt.toString()
                        }
                        answerOptions.add(randomNumInt.toString())
                    }

                    stringSplit = line.split("=").toMutableList()
                    stringSplit[stringSplit.indexOf(" ")] = " $randomNumInt"
                    intsUsed += 1

                    answerOptions.add(
                        (Random.nextInt(
                            -100,
                            100
                        )).toString()
                    ) // add another random number

                    argsInserted.add(stringSplit.joinToString("="))
                }
                regVarI.matches(line) -> { // line needs integer value for i index assignment
                    var intI = Random.nextInt(-25, 50) // value of i

                    if (snippetNum == 17) { // "while i > 0"
                        correctAnswer = if (intI <= 0) {
                            "0"
                        } else {
                            intI.toString()
                        }
                        answerOptions.add(0, correctAnswer); answerOptions.add(
                            1,
                            (correctAnswer.toInt() - 1).toString()
                        ); answerOptions.add(
                            2,
                            (correctAnswer.toInt() + 2).toString()
                        ); answerOptions.add(3, (correctAnswer.toInt() + 1).toString())
                    }

                    stringSplit = line.split("=").toMutableList()
                    stringSplit[stringSplit.indexOf(" ")] = " $intI"

                    argsInserted.add(stringSplit.joinToString("="))
                }
                regWhileI.matches(line) -> { // line needs integer value for "while i < INT" or similar
                    var whileInt = Random.nextInt(1, 20)

                    stringSplit = line.split(" ").toMutableList()
                    stringSplit[stringSplit.indexOf(":")] = "${whileInt}:"

                    if (snippetNum == 16 || snippetNum == 15 || snippetNum == 14) { // "while i < INT"
                        correctAnswer = if (intI > whileInt) {
                            "0"
                        } else {
                            (round(((whileInt - intI) / (snippetNum - 13)).toDouble())).toInt()
                                .toString() // calculate number of iterations (snippetNum minus 13 is to get the value of 1, 2 or 3 depending on the question)
                        }
                    } else if (snippetNum == 9 || snippetNum == 8 || snippetNum == 4) { // "while i < INT"
                        correctAnswer = whileInt.toString()
                    } else if (snippetNum == 18) { // "while i <= INT"
                        correctAnswer = when {
                            intI > whileInt -> {
                                "0"
                            }
                            intI == whileInt -> {
                                "1"
                            }
                            else -> {
                                ((whileInt - intI) + 1).toString() // calculate iterations
                            }
                        }
                    }

                    answerOptions.add(0, correctAnswer); answerOptions.add(
                        1,
                        (correctAnswer.toInt() - 1).toString()
                    ); answerOptions.add(
                        2,
                        (correctAnswer.toInt() + 2).toString()
                    ); answerOptions.add(3, (correctAnswer.toInt() + 1).toString())

                    argsInserted.add(stringSplit.joinToString(" "))
                }
                regSleep.matches(line) -> { // lines needs integer value for seconds to sleep
                    stringSplit = line.split("(").toMutableList()

                    var randomNumInt = Random.nextInt(1, 30)
                    correctAnswer = randomNumInt.toString() // number of seconds

                    stringSplit[stringSplit.indexOf(")")] =
                        "${randomNumInt * 1000})" // converted to milliseconds

                    answerOptions.add(0, correctAnswer); answerOptions.add(
                        1,
                        (randomNumInt * 100).toString()
                    ); answerOptions.add(2, (randomNumInt * 10).toString()); answerOptions.add(
                        3,
                        (randomNumInt * 1000).toString()
                    ) // add 4 answer options - the correct number of seconds multiplied by different powers of 10

                    argsInserted.add(stringSplit.joinToString("("))
                }
                else -> { // no regex match - no arguments required
                    argsInserted.add(line)
                }
            }
        }
        return argsInserted.joinToString("\n") // combine code snippet with all the arguments inserted
    }

    private fun removeQuotation(mString: String): String { // remove the quotation marks from string values - for placing in button text
        return mString.slice(1 until mString.length - 1)
    }

    private fun setBarTitle(numCurrentQ: Int, numQs: Int) { // change the text in the nav bar
        (activity as AppCompatActivity).supportActionBar?.title = getString(
            R.string.question_title,
            numCurrentQ + 1,
            mNumQsTotal
        )
    }
}
