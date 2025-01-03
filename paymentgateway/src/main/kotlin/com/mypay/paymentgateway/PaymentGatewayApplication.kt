package com.mypay.paymentgateway

import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.cqrs.core.infrastructure.CommandDispatcher
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.ports.driver.*
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

    @PostConstruct
    fun registerCommands() {
        commandDispacther.registerHandler(
            AuthorizeCommand::class.java, AuthorizeCommandHandler(eventSourcingHandler))
        commandDispacther.registerHandler(
            CaptureCommand::class.java, CaptureCommandHandler(eventSourcingHandler))
        commandDispacther.registerHandler(
            RefundCommand::class.java, RefundCommandHandler(eventSourcingHandler))
    }
}

fun main(args: Array<String>) {
    runApplication<PaymentGatewayApplication>(*args)
}