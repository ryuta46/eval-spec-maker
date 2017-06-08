package com.ryuta46.evalspecmaker

object Main {

    @Throws(java.io.IOException::class, org.apache.poi.openxml4j.exceptions.InvalidFormatException::class)
    @JvmStatic fun main(args: Array<String>) {


        val rootItem = MarkdownParser.parse(args[0])
        rootItem.printInformation(0)

        val creator = XlsxCreator()
        creator.createXlsx(args[1], rootItem)


    }

}
