package com.mypay.paymentqueries.adapters.kafka.events

import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentqueries.adapters.kafka.events.dto.AggregateID
import com.mypay.paymentqueries.adapters.kafka.events.dto.Money
import java.io.Serializable

class PaymentRefunded(
    aggregateID: AggregateID,
    version: Int,
    val refundAmount: Money
) : Serializable,
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "PaymentRefunded(refundAmount=$refundAmount) ${super.toString()}"
    }
}
