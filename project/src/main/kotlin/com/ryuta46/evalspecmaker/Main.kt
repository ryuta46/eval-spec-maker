package com.ryuta46.evalspecmaker

import com.ryuta46.evalspecmaker.util.Logger

object Main {
    val logger = Logger(this.javaClass.simpleName)

    @Throws(java.io.IOException::class, org.apache.poi.openxml4j.exceptions.InvalidFormatException::class)
    @JvmStatic fun main(args: Array<String>) {
        Logger.level = Logger.LOG_LEVEL_INFO

        logger.trace {
            val rootItem = MarkdownParser.parse(args[0])
            //rootItem.printInformation(0)

            val creator = XlsxCreator()
            creator.createXlsx(args[1], rootItem)
        }
    }

}
