package com.mypay.paymentgateway

import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.cqrs.core.infrastructure.CommandDispatcher
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.ports.driver.AuthorizeCommand
import com.mypay.paymentgateway.domain.ports.driver.AuthorizeCommandHandler
import com.mypay.paymentgateway.domain.ports.driver.CaptureCommand
import com.mypay.paymentgateway.domain.ports.driver.CaptureCommandHandler
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
    private lateinit var paymentProcessor: PaymentProcessor

    @PostConstruct
    fun registerCommands() {
        commandDispacther.registerHandler(
            AuthorizeCommand::class.java, AuthorizeCommandHandler(eventSourcingHandler, paymentProcessor))
        commandDispacther.registerHandler(
            CaptureCommand::class.java, CaptureCommandHandler(eventSourcingHandler))
    }
}

fun main(args: Array<String>) {
    runApplication<PaymentGatewayApplication>(*args)
}