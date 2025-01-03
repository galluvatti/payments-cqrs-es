package com.mypay.paymentgateway.domain.ports.driver

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.errors.DomainError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RefundCommandHandler(
    @Autowired
    private val eventSourcingHandler: EventSourcingHandler<Payment>
) : CommandHandler<RefundCommand> {

    override fun handle(command: RefundCommand): Result<Unit, DomainError> {
        val aggregate = eventSourcingHandler.getById(command.aggregateID)
        var result = aggregate.refund(command)
        eventSourcingHandler.save(aggregate)
            .onFailure { result = Err(it) }
        return result.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }
}
