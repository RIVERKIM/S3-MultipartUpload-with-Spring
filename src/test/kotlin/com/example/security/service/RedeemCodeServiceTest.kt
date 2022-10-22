package com.example.security.service

import com.example.security.domain.RedeemCode
import com.example.security.domain.RedeemCodeRepository
import com.example.security.service.convert.ConvertType
import com.example.security.utils.RedeemCodeGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldNotBe
import org.springframework.boot.test.context.SpringBootTest
import java.lang.Thread.sleep
import java.time.LocalDateTime
@SpringBootTest
class RedeemCodeServiceTest(
    private val redeemCodeService: RedeemCodeService,
    private val redeemCodeRepository: RedeemCodeRepository,
) : BehaviorSpec() {

    init {
        Given("30만 개의 상품권이 저장되어 있을 때") {
            RedeemCodeGenerator.generate(100000).map {
                RedeemCode(null, it, 10000, "test")
            }.run {
                redeemCodeRepository.saveAll(this)
            }

            When("RedeemCodeService에 문서 생성 요청을 보내면") {
                val url = redeemCodeService.publish(ConvertType.CSV)
                sleep(3000)
                Then("생성된 문서의 S3 URL이 반환된다.") {
                    url shouldNotBe null
                }
            }
        }
    }
}
