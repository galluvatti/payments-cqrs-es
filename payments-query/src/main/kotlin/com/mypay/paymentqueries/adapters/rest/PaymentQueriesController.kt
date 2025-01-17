package com.mypay.paymentqueries.adapters.rest

import com.mypay.paymentqueries.application.handlers.GetAuthorizedPaymentsByAmountHandler
import com.mypay.paymentqueries.application.handlers.GetCapturedPaymentsByMerchantIdHandler
import com.mypay.paymentqueries.application.queries.EqualityType
import com.mypay.paymentqueries.application.queries.GetAuthorizedPaymentsByAmount
import com.mypay.paymentqueries.application.queries.GetCapturedPaymentsByMerchantId
import com.mypay.paymentqueries.domain.Payment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/payments"])
class PaymentQueriesController(
    @Autowired private val paymentsByMerchantIdHandler: GetCapturedPaymentsByMerchantIdHandler,
    @Autowired private val authorizedPaymentsByAmountHandler: GetAuthorizedPaymentsByAmountHandler
) {
    private val logger = LoggerFactory.getLogger(PaymentQueriesController::class.java)

    @GetMapping(value = ["/captured"])
    fun getCapturedPaymentsByMerchantId(@RequestParam(value = "merchantId") merchantId: String): ResponseEntity<List<Payment>> {
        logger.info("New request to get captured payments for merchant $merchantId")
        val payments: List<Payment> = paymentsByMerchantIdHandler.handle(GetCapturedPaymentsByMerchantId(merchantId))
        return ResponseEntity.ok().body(payments)
    }

    @GetMapping(value = ["/authorized/with-amount/{equality-type}"])
    fun getAuthorizedPaymentsByAmount(
        @PathVariable(value = "equality-type") equalityType: EqualityType,
        @RequestParam(value = "amount") amount: Double
    ): ResponseEntity<List<Payment>> {
        logger.info("New request to get authorized payments with amount $equalityType $amount")
        val payments: List<Payment> = authorizedPaymentsByAmountHandler
            .handle(GetAuthorizedPaymentsByAmount(equalityType, amount))
        return ResponseEntity.ok().body(payments)
    }
}