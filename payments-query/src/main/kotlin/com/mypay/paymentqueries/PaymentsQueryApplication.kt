package com.mypay.paymentqueries

import com.mypay.cqrs.core.infrastructure.QueryDispatcher
import com.mypay.paymentqueries.application.handlers.GetCapturedPaymentsByMerchantIdHandler
import com.mypay.paymentqueries.application.queries.GetCapturedPaymentsByMerchantId
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentsQueryApplication {
    @Autowired
    lateinit var queryDispatcher: QueryDispatcher
    @Autowired
    lateinit var getCapturedPaymentsByMerchantIdHandler: GetCapturedPaymentsByMerchantIdHandler

    @PostConstruct
    fun registerHandlers() {
        queryDispatcher.registerHandler(
            GetCapturedPaymentsByMerchantId::class.java,
            getCapturedPaymentsByMerchantIdHandler
        )
    }
}


fun main(args: Array<String>) {
    runApplication<PaymentsQueryApplication>(*args)
}
