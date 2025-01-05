package com.mypay.paymentgateway

import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.cqrs.core.infrastructure.CommandDispatcher
import com.mypay.paymentgateway.application.commands.AuthorizePayment
import com.mypay.paymentgateway.application.commands.CapturePayment
import com.mypay.paymentgateway.application.commands.RefundPayment
import com.mypay.paymentgateway.application.handlers.AuthorizePaymentHandler
import com.mypay.paymentgateway.application.handlers.CapturePaymentHandler
import com.mypay.paymentgateway.application.handlers.RefundPaymentHandler
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
            AuthorizePayment::class.java, AuthorizePaymentHandler(
                eventSourcingHandler,
                InMemoryBlacklistFraudInvestigator(
                    listOf(CreditCard.Pan("4242424242424242")),
                    listOf(Email("fraud@mail.com"))
                )
            )
        )
        commandDispacther.registerHandler(
            CapturePayment::class.java, CapturePaymentHandler(eventSourcingHandler, merchantFees)
        )
        commandDispacther.registerHandler(
            RefundPayment::class.java, RefundPaymentHandler(eventSourcingHandler, refundPolicy)
        )
    }
}

fun main(args: Array<String>) {
    runApplication<PaymentGatewayApplication>(*args)
}