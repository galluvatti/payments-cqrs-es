package com.mypay.paymentgateway.domain.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.payment.Money

class PaymentCaptureFailed(
    aggregateID: AggregateID,
    version: Int,
    private val captureAmount: Money
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "PaymentCaptureFailed(captureAmount=$captureAmount) ${super.toString()}"
    }
}
