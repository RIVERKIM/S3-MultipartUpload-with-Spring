package com.example.security.presentation

import com.example.security.service.RedeemCodeService
import com.example.security.service.convert.ConvertType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class RedeemCodeController(
    private val redeemCodeService: RedeemCodeService
) {
    @PostMapping("/redeem-code/")
    fun charge(@RequestParam convertCode: String) =
        ResponseEntity.ok(redeemCodeService.publish(ConvertType.of(convertCode)))
}
