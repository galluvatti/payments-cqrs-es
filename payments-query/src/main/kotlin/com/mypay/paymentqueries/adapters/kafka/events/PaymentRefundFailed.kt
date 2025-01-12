package com.mypay.paymentqueries.adapters.kafka.events

import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentqueries.adapters.kafka.events.dto.AggregateID
import com.mypay.paymentqueries.adapters.kafka.events.dto.Money

class PaymentRefundFailed(
    aggregateID: AggregateID,
    version: Int,
    val refundAmount: Money
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "PaymentRefundFailed(refundAmount=$refundAmount) ${super.toString()}"
    }
}
