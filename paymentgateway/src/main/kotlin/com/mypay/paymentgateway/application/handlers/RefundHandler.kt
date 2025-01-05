package com.mypay.paymentgateway.application.handlers

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.application.commands.Refund
import com.mypay.paymentgateway.domain.errors.DomainError
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.services.RefundPolicy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RefundHandler(
    @Autowired private val eventSourcingHandler: EventSourcingHandler<Payment>,
    @Autowired private val refundPolicy: RefundPolicy
) : CommandHandler<Refund> {

    override fun handle(command: Refund): Result<Unit, DomainError> {
        val aggregate = eventSourcingHandler.getById(command.aggregateID)
        var result = aggregate.refund(command.refundAmount, refundPolicy)
        eventSourcingHandler.save(aggregate)
            .onFailure { result = Err(it) }
        return result.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }
}
