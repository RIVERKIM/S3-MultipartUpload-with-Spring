package com.example.security.service.convert

import com.example.security.domain.RedeemCode
import com.example.security.service.convert.strategy.RedeemCodeConvertStrategy
import org.springframework.stereotype.Component

@Component
class RedeemCodeToDocumentConverter(
    redeemCodeConvertStrategies: List<RedeemCodeConvertStrategy>
) {
    private val map: Map<ConvertType, RedeemCodeConvertStrategy> =
        redeemCodeConvertStrategies.associateBy { it.type() }

    fun convert(redeemCodes: List<RedeemCode>, convertType: ConvertType) =
        findStrategy(convertType)?.convert(redeemCodes) ?: throw IllegalStateException("no type : $convertType")

    private fun findStrategy(convertType: ConvertType) =
        map[convertType]
}
