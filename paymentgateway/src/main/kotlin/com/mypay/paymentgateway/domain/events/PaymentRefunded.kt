package com.mypay.paymentgateway.domain.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.payment.Money

class PaymentRefunded(
    aggregateID: AggregateID,
    version: Int,
    val refundAmount: Money
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "PaymentRefunded(refundAmount=$refundAmount) ${super.toString()}"
    }
}
