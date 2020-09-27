package com.example.microbitlearner

// class for hard-coded questions that are used in the typing questions quiz
class TypeQuestion(question: Int, ans: Int, hint: Int, inputMthd: Int) {

    // initialise
    var mQuestion: Int = question // question text
    var mAns: Int = ans // answer text
    var mHint: Int = hint // hint text
    var mInputMthd: Int = inputMthd // input method - number or text

    fun getQuestion(): Int {
        return mQuestion
    }

    fun setQuestion(question: Int) {
        mQuestion = question
    }

    fun getInputMthd(): Int {
        return mInputMthd
    }

    fun setInputMthd(inputMthd: Int) {
        mInputMthd = inputMthd
    }

    fun getAnswer(): Int {
        return mAns
    }

    fun setAnsQuestion(ansQuestion: Int) {
        mAns = ansQuestion
    }

    fun getHint(): Int {
        return mHint
    }

    fun setHint(hint: Int) {
        mHint = hint
    }
}