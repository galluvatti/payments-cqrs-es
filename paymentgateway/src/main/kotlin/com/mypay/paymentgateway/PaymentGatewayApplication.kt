package com.mypay.paymentgateway

import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.cqrs.core.infrastructure.CommandDispatcher
import com.mypay.paymentgateway.application.commands.Authorize
import com.mypay.paymentgateway.application.commands.Capture
import com.mypay.paymentgateway.application.commands.Refund
import com.mypay.paymentgateway.application.handlers.AuthorizeHandler
import com.mypay.paymentgateway.application.handlers.CaptureHandler
import com.mypay.paymentgateway.application.handlers.RefundHandler
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.services.InMemoryBlacklistFraudInvestigator
import com.mypay.paymentgateway.domain.payment.billing.Email
import com.mypay.paymentgateway.domain.payment.creditcard.CreditCard
import com.mypay.paymentgateway.domain.services.MerchantFees
import com.mypay.paymentgateway.domain.services.RefundPolicy
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaymentGatewayApplication {
    @Autowired
    private lateinit var commandDispacther: CommandDispatcher
    @Autowired
    private lateinit var eventSourcingHandler: EventSourcingHandler<Payment>
    @Autowired
    private lateinit var refundPolicy: RefundPolicy
    @Autowired
    private lateinit var merchantFees: MerchantFees

    @PostConstruct
    fun registerCommands() {
        commandDispacther.registerHandler(
            Authorize::class.java, AuthorizeHandler(
                eventSourcingHandler,
                InMemoryBlacklistFraudInvestigator(
                    listOf(CreditCard.Pan("4242424242424242")),
                    listOf(Email("fraud@mail.com"))
                )
            )
        )
        commandDispacther.registerHandler(
            Capture::class.java, CaptureHandler(eventSourcingHandler, merchantFees)
        )
        commandDispacther.registerHandler(
            Refund::class.java, RefundHandler(eventSourcingHandler, refundPolicy)
        )
    }
}

fun main(args: Array<String>) {
    runApplication<PaymentGatewayApplication>(*args)
}