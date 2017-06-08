package com.ryuta46.evalspecmaker

import com.ryuta46.evalspecmaker.util.Logger
import org.pegdown.PegDownProcessor
import org.pegdown.ast.*

import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Locale

object MarkdownParser {
    val logger = Logger(this.javaClass.simpleName)

    @Throws(IOException::class)
    internal fun parse(file: String): TestItem {
        logger.trace {
            val inputMarkdown = File(file)
            val buff = CharArray(inputMarkdown.length().toInt())

            FileReader(inputMarkdown).use { reader ->
                reader.read(buff)
                val processor = PegDownProcessor()
                val rootNode = processor.parseMarkdown(buff)

                val rootItem = TestItem().apply { level = 0 }

                convertToTestItem(rootNode, mutableListOf(rootItem))
                return rootItem
            }
        }
    }


    private fun convertToTestItem(node: Node, parentStack: MutableList<TestItem>) {
        logger.trace {
            if (node is HeaderNode) {
                val nodeLevel = node.level
                logger.i(String.format(Locale.JAPAN, "h%d", nodeLevel))

                // 自分より階層が上の直近のノードを親する
                parentStack.removeAll{ it.level >= nodeLevel }
                val target = parentStack.lastOrNull() ?: return

                val newItem = TestItem().apply { level = nodeLevel }
                target.addChild(newItem)
                parentStack.add(newItem)
            } else {
                val target = parentStack.lastOrNull() ?: return
                when(node) {
                    is TextNode -> {
                        val text = node.text.trim { it <= ' ' }
                        if (!text.isEmpty()) {
                            logger.i(String.format(Locale.JAPAN, "text:%s", text))
                            target.addText(text)
                        }
                    }
                    is ListItemNode -> target.setAddTarget(TestItem.TextContainer.METHOD)
                    is RefLinkNode -> target.setAddTarget(TestItem.TextContainer.CONFIRM)
                    else -> logger.d("Class:" + node.javaClass.name)
                }
            }

            for (childNode in node.children) {
                convertToTestItem(childNode, parentStack)
            }
        }
    }

    private fun printNode(node: Node, level: Int) {
        logger.trace {
            val builder = StringBuilder()
            for (i in 0..level - 1) {
                builder.append("-")
            }
            val prefix = builder.toString()

            println(prefix + node.javaClass.name)

            for (childNode in node.children) {
                printNode(childNode, level+1)
            }
        }
    }

}
