package com.example.security.service.convert

enum class ConvertType(
    val code: String,
    val format: String
) {
    EXCEL("01", "EXCEL"),
    CSV("02", "CSV");

    companion object {
        fun of(code: String?): ConvertType {
            if(code == null) {
                throw IllegalArgumentException("잘못된 코드 입니다.")
            }

            return ConvertType.values()
                .first { it.code == code }
        }
    }
}
