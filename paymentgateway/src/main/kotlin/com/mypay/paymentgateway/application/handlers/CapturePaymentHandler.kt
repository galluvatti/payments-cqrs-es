package com.mypay.paymentgateway.application.handlers

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.application.commands.CapturePayment
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.errors.DomainError
import com.mypay.paymentgateway.domain.services.MerchantFees
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CapturePaymentHandler(
    @Autowired
    private val eventSourcingHandler: EventSourcingHandler<Payment>,
    @Autowired
    private val merchantFees: MerchantFees
) : CommandHandler<CapturePayment> {

    override fun handle(command: CapturePayment): Result<Unit, DomainError> {
        val aggregate = eventSourcingHandler.getById(command.aggregateID)
        var result = aggregate.capture(command.captureAmount, merchantFees)
        eventSourcingHandler.save(aggregate)
            .onFailure { result = Err(it) }
        return result.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }
}
