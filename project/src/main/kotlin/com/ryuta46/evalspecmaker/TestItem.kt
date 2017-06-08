package com.ryuta46.evalspecmaker

internal class TestItem {
    internal enum class TextContainer {
        // 項目本文
        BODY,
        // 試験手順
        METHOD,
        // 確認点
        CONFIRM
    }

    private val mChildren = java.util.ArrayList<TestItem>()
    private val mBodies = java.util.ArrayList<String>()
    private val mMethods = java.util.ArrayList<String>()
    private val mConfirms = java.util.ArrayList<String>()
    var parent: TestItem? = null
        private set

    private var mAddTarget = TextContainer.BODY

    var level = 0

    fun addText(text: String) {
        when (mAddTarget) {
            TextContainer.BODY -> mBodies.add(text)
            TextContainer.METHOD -> {
                val ind = mMethods.size + 1
                mMethods.add(ind.toString() + ". " + text)
            }
            TextContainer.CONFIRM -> mConfirms.add("・" + text)
        }
    }

    val bodies: String
        get() = textListToString(mBodies)
    val methods: String
        get() = textListToString(mMethods)
    val confirms: String
        get() = textListToString(mConfirms)

    private fun textListToString(list: List<String>): String {
        if (list.isEmpty()) {
            return ""
        }
        val builder = StringBuilder(list[0])
        for (i in 1..list.size - 1) {
            builder.append("\n")
            builder.append(list[i])
        }
        return builder.toString()
    }

    fun addChild(child: TestItem) {
        mChildren.add(child)
        child.parent = this
    }

    fun setAddTarget(target: TextContainer) {
        mAddTarget = target
    }

    val childCount: Int
        get() = mChildren.size

    fun getChild(index: Int): TestItem {
        return mChildren[index]
    }


    fun printInformation(level: Int) {
        val builder = StringBuilder()
        for (i in 0..level - 1) {
            builder.append("-")
        }
        val prefix = builder.toString()


        println(String.format("%sL:%d", prefix, level))
        for (text in mBodies) {
            println(String.format("%sT:%s", prefix, text))
        }
        for (text in mMethods) {
            println(String.format("%sM:%s", prefix, text))
        }
        for (text in mConfirms) {
            println(String.format("%sC:%s", prefix, text))
        }

        for (childNode in mChildren) {
            childNode.printInformation(level + 1)
        }
    }


}
