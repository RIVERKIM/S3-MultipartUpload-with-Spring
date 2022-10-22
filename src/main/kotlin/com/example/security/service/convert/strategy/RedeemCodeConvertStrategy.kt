package com.example.security.service.convert.strategy

import com.example.security.domain.RedeemCode
import com.example.security.service.convert.ConvertType
import kotlin.reflect.full.declaredMemberProperties

interface RedeemCodeConvertStrategy {

    fun convert(redeemCodes: List<RedeemCode>): ByteArray

    fun type(): ConvertType

    fun getRedeemCodeHeaderTitles(): Array<String> {
        return RedeemCode::class.declaredMemberProperties
            .map {
                it.name
            }
            .filter{ it != "id" }
            .toTypedArray()
    }
}
