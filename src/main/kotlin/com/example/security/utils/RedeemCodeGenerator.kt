package com.example.security.utils

import java.util.concurrent.ThreadLocalRandom

private const val REDEEM_CODE_SIZE: Int = 16

object RedeemCodeGenerator {
    private val numUpperChars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    fun generate(count: Long): Set<String> {
        val codes = mutableSetOf<String>()
        val code = StringBuilder()
        do {
            repeat((1..REDEEM_CODE_SIZE).count()) {
                getRandomIndex().apply {
                    code.append(numUpperChars[this])
                }
            }
            codes.add(code.toString())
            code.clear()
        } while (!isGeneratedCountEnough(codes, count))

        return codes
    }

    private fun getRandomIndex() = ThreadLocalRandom.current().nextInt(0, numUpperChars.size)

    private fun isGeneratedCountEnough(codes: Set<String>, count: Long) = codes.size >= count
}

