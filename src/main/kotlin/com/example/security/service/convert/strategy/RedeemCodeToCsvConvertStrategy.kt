package com.example.security.service

import com.example.security.domain.RedeemCode
import com.example.security.service.convert.ConvertType
import com.example.security.service.convert.strategy.RedeemCodeConvertStrategy
import com.example.security.service.convert.generator.csv.CsvGenerator
import com.example.security.service.convert.generator.csv.CsvRow
import org.springframework.stereotype.Component

@Component
class RedeemCodeToCsvConverter(
    private val csvGenerator: CsvGenerator
) : RedeemCodeConvertStrategy {

    override fun convert(redeemCodes: List<RedeemCode>): ByteArray {
        val csvRows = redeemCodes.map {
            CsvRow(
                it.redeemCode,
                it.price.toString(),
                it.reason
            )
        }
        return csvGenerator.generate(getRedeemCodeHeaderTitles(), csvRows)
    }

    override fun type() = ConvertType.CSV
}
