package com.ryuta46.evalspecmaker

import org.pegdown.PegDownProcessor
import org.pegdown.ast.*

import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Locale

object MarkdownParser {

    @Throws(IOException::class)
    internal fun parse(file: String): TestItem {
        var reader: FileReader? = null

        try {
            val inputMarkdown = File(file)
            val buff = CharArray(inputMarkdown.length().toInt())

            reader = FileReader(inputMarkdown)
            val readSize = reader.read(buff)
            //if (readSize != inputMarkdown.length()) {
            //    throw new IOException("Invalid read size");
            //}

            val processor = PegDownProcessor()
            val rootNode = processor.parseMarkdown(buff)
            val rootItem = TestItem()
            rootItem.level = 0
            convertToTestItem(rootNode, rootItem)

            return rootItem

        } finally {
            if (reader != null) {
                reader.close()
            }
        }
        //printNodes(rootNode, 0);
    }


    private fun convertToTestItem(node: Node, parent: TestItem): TestItem {
        var parent = parent
        var newParent: TestItem = parent
        if (node is HeaderNode) {
            val headerNode = node
            println(String.format(Locale.JAPAN, "h%d", headerNode.level))
            val level = headerNode.level

            val newItem = TestItem()
            newItem.level = level

            // 自分のlevel-1 の親を探す.
            while (parent != null && parent.level > level - 1) {
                // ルートノードを超えて親を探索することは無いはずなので、この処理はフェールセーフ
                if (parent.parent == null) {
                    System.err.println("Error!! Failed to search parent.")
                    return newParent
                }
                parent = parent.parent!!
            }

            parent.addChild(newItem)
            newParent = newItem
        } else if (node is TextNode) {
            val text = node.text.trim { it <= ' ' }

            if (!text.isEmpty()) {
                println(String.format(Locale.JAPAN, "text:%s", text))
                parent.addText(text)
            }

        } else if (node is ListItemNode) {
            parent.setAddTarget(TestItem.TextContainer.METHOD)
        } else if (node is RefLinkNode) {
            parent.setAddTarget(TestItem.TextContainer.CONFIRM)
        } else {
            println("Class:" + node.javaClass.name)
        }

        for (childNode in node.children) {
            newParent = convertToTestItem(childNode, newParent)
        }

        return newParent

    }

}
