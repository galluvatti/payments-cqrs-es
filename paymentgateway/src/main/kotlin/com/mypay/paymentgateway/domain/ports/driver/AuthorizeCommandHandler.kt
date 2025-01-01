package com.mypay.paymentgateway.domain.ports.driver

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.errors.DomainError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthorizeCommandHandler(
    @Autowired private val eventSourcingHandler: EventSourcingHandler<Payment>
) : CommandHandler<AuthorizeCommand> {

    //TODO Test
    override fun handle(command: AuthorizeCommand): Result<Unit, DomainError> {
        val aggregate = Payment(command.aggregateID)
        var result = aggregate.authorize(command)
        eventSourcingHandler.save(aggregate)
            .onFailure { result = Err(it) }
        return result.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }
}
