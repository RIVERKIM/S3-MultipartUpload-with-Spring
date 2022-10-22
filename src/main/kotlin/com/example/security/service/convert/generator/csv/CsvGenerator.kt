package com.example.security.service.convert.generator.csv

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.springframework.stereotype.Component

@Component
class CsvGenerator {
    fun generate(headerTitles: Array<String>, rows: List<CsvRow>): ByteArray {
        return CSVPrinter(StringBuilder(), CSVFormat.DEFAULT.withHeader(*headerTitles)).use { csvPrinter ->
            rows.forEach { csvPrinter.printRecord(it.data) }
            csvPrinter.out.toString().toByteArray()
        }
    }
}
