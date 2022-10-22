package com.example.security.service.convert.generator.csv

data class CsvRow(
    val data: List<String>
) {
    constructor(vararg data: String): this(data.toList())
}
