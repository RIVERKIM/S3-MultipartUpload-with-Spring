package com.example.security.utils

import java.time.LocalDate
import java.time.LocalTime

object S3KeyGenerator {
    private const val DEFAULT_FOLDER = "Redeem Code/%s/%s"

    fun generateKey() =
        String.format(DEFAULT_FOLDER, LocalDate.now().toString(), LocalTime.now().toString())
}
