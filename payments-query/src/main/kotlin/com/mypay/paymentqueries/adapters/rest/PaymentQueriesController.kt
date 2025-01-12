package com.mypay.paymentqueries.adapters.rest

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/payments"])
class PaymentQueriesController {
    private val logger = LoggerFactory.getLogger(PaymentQueriesController::class.java)
}