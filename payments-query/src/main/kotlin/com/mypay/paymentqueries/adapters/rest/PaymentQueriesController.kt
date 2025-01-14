package com.mypay.paymentqueries.adapters.rest

import com.mypay.cqrs.core.infrastructure.QueryDispatcher
import com.mypay.paymentqueries.application.queries.GetCapturedPaymentsByMerchantId
import com.mypay.paymentqueries.domain.Payment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/payments"])
class PaymentQueriesController(
    @Autowired private val queryDispatcher: QueryDispatcher
) {
    private val logger = LoggerFactory.getLogger(PaymentQueriesController::class.java)

    @GetMapping(value = ["/captured"])
    fun getCapturedPaymentsByMerchantId(@RequestParam(value = "merchantId") merchantId: String): ResponseEntity<List<Payment>> {
        logger.info("New request to get captured payments for merchant $merchantId")
        val payments = queryDispatcher.send<Payment>(GetCapturedPaymentsByMerchantId(merchantId))
        return ResponseEntity.ok().body(payments)
    }
}