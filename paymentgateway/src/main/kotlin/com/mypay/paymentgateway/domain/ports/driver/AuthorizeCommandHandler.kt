package com.mypay.paymentgateway.domain.ports.driver

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapBoth
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.errors.PaymentGatewayError
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthorizeCommandHandler(
    @Autowired private val eventSourcingHandler: EventSourcingHandler<Payment>,
    @Autowired private val paymentProcessor: PaymentProcessor
) : CommandHandler<AuthorizeCommand> {

    //TODO Test
    override fun handle(command: AuthorizeCommand): Result<Unit, PaymentGatewayError> {
        val aggregate = Payment(command.aggregateID, paymentProcessor)
        val result = aggregate.authorize(command)
        eventSourcingHandler.save(aggregate)
        return result.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }
}
