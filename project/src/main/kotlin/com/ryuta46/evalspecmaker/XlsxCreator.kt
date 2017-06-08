package com.ryuta46.evalspecmaker

import com.ryuta46.evalspecmaker.util.Logger
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class XlsxCreator {
    val logger = Logger(this.javaClass.simpleName)

    companion object {
        // 列幅最大値
        private val MAX_WIDTH = 10000
        // 推定折り返し文字数
        private val WRAP_CHAR_LENGTH = 20
        // ヘッダ行のインデックス
        private val HEADER_INDEX = 1
    }

    private val book: Workbook

    // デフォルトのスタイル
    private val cellStyle: CellStyle
    // 下の枠なし
    private val cellStyleNoBottomBorder: CellStyle
    // 上下の枠なし
    private val cellStyleNoVerticalBorder: CellStyle
    // 中央寄せ
    private val cellStyleCenter: CellStyle
    // ヘッダのスタイル
    private val cellStyleHeader: CellStyle

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
        book = XSSFWorkbook()
        cellStyle = book.createCellStyle().apply {
            alignment = CellStyle.ALIGN_LEFT
            verticalAlignment = CellStyle.VERTICAL_TOP
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
            alignment = CellStyle.ALIGN_CENTER
            verticalAlignment = CellStyle.VERTICAL_CENTER
            // 何故か以下行を呼び出さないと中央揃えにならない
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
        }


        cellStyleHeader = book.createCellStyle().apply {
            cloneStyleFrom(cellStyle)
            alignment = CellStyle.ALIGN_CENTER
            verticalAlignment = CellStyle.VERTICAL_CENTER
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
            fillPattern = CellStyle.SOLID_FOREGROUND
        }
    }


    @Throws(java.io.IOException::class)
    internal fun createXlsx(file: String, rootItem: TestItem) {
        logger.trace {
            rootItem.children.forEach { createCategory(it) }

            java.io.FileOutputStream(file).use {
                book.write(it)
            }
        }
    }

    private fun createCategory(categoryItem: TestItem) {
        val sheet = book.createSheet(categoryItem.bodies)
        // グリッド表示を消す.
        sheet.isDisplayGridlines = false

        setColumnHeader(sheet)

        var rowIndex = HEADER_INDEX + 1

        categoryItem.children.forEach { major ->
            val majorBody = major.bodies
            var majorLineCount = estimateLineCount(majorBody)
            setCellValue(sheet, Column.MAJOR.index, rowIndex, majorBody).cellStyle = cellStyleNoBottomBorder

            major.children.forEachIndexed { middleIndex, middle ->
                val middleBody = middle.bodies
                var middleLineCount = estimateLineCount(middleBody)

                setCellValue(sheet, Column.MIDDLE.index, rowIndex, middleBody).cellStyle = cellStyleNoBottomBorder

                middle.children.forEachIndexed { minorIndex, minor ->
                    // 中項目、小項目のみが進んだ場合は大項目の縦線なし
                    if (middleIndex > 0 || minorIndex > 0) {
                        setCellValue(sheet, Column.MAJOR.index, rowIndex, "").cellStyle = cellStyleNoVerticalBorder
                        majorLineCount = 1
                    }
                    // 小項目のみが進んだ場合は中項目の縦線なし
                    if (minorIndex > 0) {
                        setCellValue(sheet, Column.MIDDLE.index, rowIndex, "").cellStyle = cellStyleNoVerticalBorder
                        middleLineCount = 1
                    }

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

    private fun setCellValue(sheet: Sheet, columnIndex: Int, rowIndex: Int, text: String): Cell {
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

    private fun setColumnHeader(sheet: Sheet) {
        Column.values().forEach { column ->
            setCellValue(sheet, column.index, Companion.HEADER_INDEX, column.title).cellStyle = cellStyleHeader
        }
    }

}
