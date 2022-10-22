package com.example.security.service.convert.strategy

import com.example.security.domain.RedeemCode
import com.example.security.service.convert.ConvertType
import com.example.security.service.convert.generator.excel.ExcelGenerator
import com.example.security.service.convert.generator.excel.ExcelRow
import org.springframework.stereotype.Component

@Component
class RedeemCodeToExcelConvertStrategy(
    private val excelGenerator: ExcelGenerator
) : RedeemCodeConvertStrategy {

    override fun convert(redeemCodes: List<RedeemCode>): ByteArray {
        val excelRows = redeemCodes.map {
            ExcelRow(
                it.redeemCode,
                it.price.toString(),
                it.reason
            )
        }
        return excelGenerator.generate(getRedeemCodeHeaderTitles(), excelRows)
    }

    override fun type() = ConvertType.EXCEL
}
