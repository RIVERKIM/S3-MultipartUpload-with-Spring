package com.example.security.service.convert.generator.excel

data class ExcelRow(
    val data: List<String>
) {
    constructor(vararg data: String): this(data.toList())
}
