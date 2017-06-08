package com.ryuta46.evalspecmaker


class XlsxCreator {

    private val book: org.apache.poi.ss.usermodel.Workbook

    // デフォルトのスタイル
    private val cellStyle: org.apache.poi.ss.usermodel.CellStyle
    // 下の枠なし
    private val cellStyleNoBottomBorder: org.apache.poi.ss.usermodel.CellStyle
    // 上下の枠なし
    private val cellStyleNoVerticalBorder: org.apache.poi.ss.usermodel.CellStyle
    // 中央寄せ
    private val cellStyleCenter: org.apache.poi.ss.usermodel.CellStyle
    // ヘッダのスタイル
    private val cellStyleHeader: org.apache.poi.ss.usermodel.CellStyle

    private enum class Column(val title: String) {
        MAJOR("大項目"),
        MIDDLE("中項目"),
        MINOR("小項目"),
        METHOD("確認手順"),
        CONFIRM("確認項目"),
        RESULT("結果"),
        DATE("試験日"),
        TESTER("試験者"),
        INFO("備考");

        val index: Int
            get() = ordinal + 1
    }



    init {
        book = org.apache.poi.xssf.usermodel.XSSFWorkbook()
        cellStyle = book.createCellStyle().apply {
            alignment = org.apache.poi.ss.usermodel.CellStyle.ALIGN_LEFT
            verticalAlignment = org.apache.poi.ss.usermodel.CellStyle.VERTICAL_TOP
            wrapText = true
            borderTop = 1.toShort()
            borderLeft = 1.toShort()
            borderRight = 1.toShort()
            borderBottom = 1.toShort()
        }

        cellStyleNoBottomBorder = book.createCellStyle().apply {
            cloneStyleFrom(cellStyle)
            borderBottom = 0.toShort()
        }

        cellStyleNoVerticalBorder = book.createCellStyle().apply {
            cloneStyleFrom(cellStyleNoBottomBorder)
            borderTop = 0.toShort()
        }

        cellStyleCenter = book.createCellStyle().apply {
            cloneStyleFrom(cellStyle)
            alignment = org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER
            verticalAlignment = org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER
            // 何故か以下行を呼び出さないと中央揃えにならない
            fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex()
        }


        cellStyleHeader = book.createCellStyle().apply {
            cloneStyleFrom(cellStyle)
            alignment = org.apache.poi.ss.usermodel.CellStyle.ALIGN_CENTER
            verticalAlignment = org.apache.poi.ss.usermodel.CellStyle.VERTICAL_CENTER
            fillForegroundColor = org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex()
            fillPattern = org.apache.poi.ss.usermodel.CellStyle.SOLID_FOREGROUND
        }
    }


    @Throws(java.io.IOException::class)
    internal fun createXlsx(file: String, rootItem: TestItem) {
        (0..rootItem.childCount - 1)
                .map { rootItem.getChild(it) }
                .forEach { createCategory(it) }

        java.io.FileOutputStream(file).use {
            book.write(it)
        }
    }

    private fun createCategory(categoryItem: TestItem) {
        val sheet = book.createSheet(categoryItem.bodies)
        // グリッド表示を消す.
        sheet.isDisplayGridlines = false

        setColumnHeader(sheet)

        var rowIndex = HEADER_INDEX + 1

        for (i in 0..categoryItem.childCount - 1) {
            // 大項目
            val major = categoryItem.getChild(i)
            val majorBody = major.bodies
            var majorLineCount = estimateLineCount(majorBody)
            setCellValue(sheet, Column.MAJOR.index, rowIndex, majorBody).cellStyle = cellStyleNoBottomBorder


            for (j in 0..major.childCount - 1) {
                // 中項目
                val middle = major.getChild(j)
                val middleBody = middle.bodies
                var middleLineCount = estimateLineCount(middleBody)

                setCellValue(sheet, Column.MIDDLE.index, rowIndex, middleBody).cellStyle = cellStyleNoBottomBorder

                for (k in 0..middle.childCount - 1) {
                    if (j > 0 || k > 0) {
                        setCellValue(sheet, Column.MAJOR.index, rowIndex, "").cellStyle = cellStyleNoVerticalBorder
                        majorLineCount = 1
                    }
                    if (k > 0) {
                        setCellValue(sheet, Column.MIDDLE.index, rowIndex, "").cellStyle = cellStyleNoVerticalBorder
                        middleLineCount = 1
                    }
                    // 小項目
                    val minor = middle.getChild(k)
                    val minorBody = minor.bodies
                    val method = minor.methods
                    val confirm = minor.confirms

                    setCellValue(sheet, Column.MINOR.index, rowIndex, minorBody)
                    // 手順
                    setCellValue(sheet, Column.METHOD.index, rowIndex, method)
                    // 確認点
                    setCellValue(sheet, Column.CONFIRM.index, rowIndex, confirm)


                    // 各列の行数の推測値から最大のものを列の高さに設定
                    var maxRowHeightUnit = Math.max(majorLineCount, middleLineCount)
                    maxRowHeightUnit = Math.max(maxRowHeightUnit, estimateLineCount(minorBody))
                    maxRowHeightUnit = Math.max(maxRowHeightUnit, estimateLineCount(method))
                    maxRowHeightUnit = Math.max(maxRowHeightUnit, estimateLineCount(confirm))

                    // 高さを反映
                    val row = sheet.getRow(rowIndex)
                    row.height = (row.height * maxRowHeightUnit).toShort()


                    // スタイルのみ設定する項目を設定
                    setCellValue(sheet, Column.RESULT.index, rowIndex, "").cellStyle = cellStyleCenter
                    setCellValue(sheet, Column.DATE.index, rowIndex, "")
                    setCellValue(sheet, Column.TESTER.index, rowIndex, "").cellStyle = cellStyleCenter
                    setCellValue(sheet, Column.INFO.index, rowIndex, "")

                    rowIndex++
                }
            }
        }

        // Resize all column
        Column.values().map { it.index }.forEach { index ->
            sheet.autoSizeColumn(index)
            if (Companion.MAX_WIDTH < sheet.getColumnWidth(index)) {
                sheet.setColumnWidth(index, Companion.MAX_WIDTH)
            }
        }
    }

    private fun setCellValue(sheet: org.apache.poi.ss.usermodel.Sheet, columnIndex: Int, rowIndex: Int, text: String): org.apache.poi.ss.usermodel.Cell {
        val row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)
        val cell = row.getCell(columnIndex) ?: row.createCell(columnIndex)

        cell.setCellValue(text)
        cell.cellStyle = cellStyle

        return cell
    }

    private fun estimateLineCount(text: String): Int {
        val lines = text.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

        val lineCount = lines
                .map { it.codePointCount(0, it.length) }
                .sumBy { (it + Companion.WRAP_CHAR_LENGTH - 1) / Companion.WRAP_CHAR_LENGTH }

        return lineCount

    }

    private fun setColumnHeader(sheet: org.apache.poi.ss.usermodel.Sheet) {
        Column.values().forEach { column ->
            setCellValue(sheet, column.index, Companion.HEADER_INDEX, column.title).cellStyle = cellStyleHeader
        }
    }

    companion object {

        // 列幅最大値
        private val MAX_WIDTH = 10000
        // 推定折り返し文字数
        private val WRAP_CHAR_LENGTH = 20

        // ヘッダ行のインデックス
        private val HEADER_INDEX = 1
    }
}
