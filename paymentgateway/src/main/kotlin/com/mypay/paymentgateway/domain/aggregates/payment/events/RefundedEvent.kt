package com.mypay.paymentgateway.domain.aggregates.payment.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.psp.RefundID

class RefundedEvent(
    aggregateID: AggregateID,
    version: Int,
    val refundAmount: Money,
    val refundID: RefundID
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "RefundedEvent(refundAmount=$refundAmount, captureID=$refundID)"
    }
}
