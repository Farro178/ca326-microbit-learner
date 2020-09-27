var mQuestionTemplate1: String = "from microbit import *\ndisplay.scroll()"
var mQuestionTemplate2: String = "from microbit import *\nwhile True:\n    if pin0.is_touched():\n        display.show(Image.)\n    else:\n        display.show(Image.)"
var mQuestionTemplate3: String = "from microbit import *\ndisplay.scroll()\nwhile True:\n    if pin0.is_touched():\n        display.show(Image.)\n    else:\n        display.show(Image.)"
var mOption1: String = "HEART"
var mOption2: String = "HAPPY"
var mOption3: String = "SAD"
var mOption4: String = "CONFUSED"
var mOption5: String = "SMILE"
var mOption6: String = "ASLEEP"
var mOption7: String = "YES"
var mOption8: String = "NO"
var mOptionsImages: MutableList<String> = mutableListOf(mOption1, mOption2, mOption3, mOption4, mOption5, mOption6, mOption7, mOption8)
var mOption1s: String = "\"Hello, world\""
var mOption2s: String = "\"Hello, World.\""
var mOption3s: String = "\"hello world\""
var mOption4s: String = "\"Hello! world!\""
var mOption5s: String = "\"hello. world.\""
var mOption6s: String = "\"Helloworld\""
var mOption7s: String = "\"Hello World\""
var mOption8s: String = "\"Hello\""
var mOptionsStrings: MutableList<String> = mutableListOf(mOption1s, mOption2s, mOption3s, mOption4s, mOption5s, mOption6s, mOption7s, mOption8s)
val regImage = """(.*)display.show\(Image.\)""".toRegex()
val regString = """(.*)display.scroll\(\)""".toRegex()

fun main(args: Array<String>) {
    var splitlist: MutableList<String>
    var newlist: MutableList<String> = mutableListOf()
    var lines: MutableList<String> = mQuestionTemplate3.split("\n").toMutableList()
    for(item in lines){
        if(regImage.matches(item)){
            splitlist = item.split(".").toMutableList()
            mOptionsImages.shuffle()
            splitlist[splitlist.indexOf("show(Image")+1] = mOptionsImages[0] + ")"
            newlist.add(splitlist.joinToString("."))
        } else if(regString.matches(item)){
            splitlist = item.split(".").toMutableList()
            mOptionsStrings.shuffle()
            splitlist[splitlist.indexOf("scroll()")] = "scroll(" + mOptionsStrings[0] + ")"
            newlist.add(splitlist.joinToString("."))
        } else {
            newlist.add(item)
        }
    }
    println(newlist.joinToString("\n"))
}