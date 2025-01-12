package com.mypay.paymentqueries.adapters.kafka.events

import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentqueries.adapters.kafka.events.dto.AggregateID
import com.mypay.paymentqueries.adapters.kafka.events.dto.Money
import java.io.Serializable
import java.time.LocalDateTime

class PaymentCaptured(
    aggregateID: AggregateID,
    version: Int,
    val captureAmount: Money,
    val fees: Double,
    val captureDate: LocalDateTime
) : Serializable,
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "PaymentCaptured(captureAmount=$captureAmount, captureDate=$captureDate) ${super.toString()}"
    }
}
