package com.mypay.paymentgateway.application.handlers

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.application.commands.AuthorizePayment
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.errors.DomainError
import com.mypay.paymentgateway.domain.services.FraudInvestigator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthorizePaymentHandler(
    @Autowired private val eventSourcingHandler: EventSourcingHandler<Payment>,
    @Autowired private val fraudInvestigator: FraudInvestigator
) : CommandHandler<AuthorizePayment> {

    override fun handle(command: AuthorizePayment): Result<Unit, DomainError> {
        val aggregate = Payment(command.aggregateID)
        var result = aggregate.authorize(
            command.merchant,
            command.authorizationAmount,
            command.cardHolder,
            command.creditCard,
            command.order,
            fraudInvestigator
        )
        eventSourcingHandler.save(aggregate)
            .onFailure { result = Err(it) }
        return result.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }
}
