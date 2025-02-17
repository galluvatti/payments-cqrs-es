package com.mypay.paymentgateway.application.handlers

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.handlers.EventSourcingRepository
import com.mypay.paymentgateway.application.commands.RefundPayment
import com.mypay.paymentgateway.domain.errors.DomainError
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.services.RefundPolicy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RefundPaymentHandler(
    @Autowired private val eventSourcingRepository: EventSourcingRepository<Payment>,
    @Autowired private val refundPolicy: RefundPolicy
) : CommandHandler<RefundPayment> {

    override fun handle(command: RefundPayment): Result<Unit, DomainError> {
        val aggregate = eventSourcingRepository.getById(command.aggregateID)
        var result = aggregate.refund(command.refundAmount, refundPolicy)
        eventSourcingRepository.save(aggregate)
            .onFailure { result = Err(it) }
        return result.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }
}
