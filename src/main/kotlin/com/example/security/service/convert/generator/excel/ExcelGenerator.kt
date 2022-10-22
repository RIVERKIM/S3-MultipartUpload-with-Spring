package com.example.security.service.convert.generator.excel

import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

@Component
class ExcelGenerator {

    fun generate(headerTitles: Array<String>, row: List<ExcelRow>): ByteArray {
        return SXSSFWorkbook().run {
            val sheet = this.createSheet()

            setHeaderTitles(this, sheet, headerTitles)
            fillData(sheet, row, headerTitles.size)

            val out = ByteArrayOutputStream()
            this.write(out)
            this.close()

            out.toByteArray()
        }
    }

    private fun setHeaderTitles(workbook: SXSSFWorkbook, sheet: SXSSFSheet, headerTitles: Array<String>) {
        val headerFont = workbook.createFont()
        headerFont.bold = true

        val headerCellStyles = workbook.createCellStyle()
        headerCellStyles.setFont(headerFont)
        headerCellStyles.fillBackgroundColor = IndexedColors.GREY_80_PERCENT.index

        val headerRow = sheet.createRow(0)

        for(col in headerTitles.indices) {
            val cell = headerRow.createCell(col)
            cell.setCellValue(headerTitles[col])
            cell.cellStyle = headerCellStyles
        }
    }

    private fun fillData(sheet: SXSSFSheet, rows: List<ExcelRow>, columnSize: Int) {
        sheet.trackAllColumnsForAutoSizing()

        rows.forEachIndexed { index, it ->
            val row = sheet.createRow(index + 1)
            val properties = it.data
            properties.forEachIndexed { propertyIndex, property ->
                row.createCell(propertyIndex).setCellValue(property)
            }
        }

        repeat(columnSize) { col ->
            sheet.autoSizeColumn(col)
        }
    }
}
