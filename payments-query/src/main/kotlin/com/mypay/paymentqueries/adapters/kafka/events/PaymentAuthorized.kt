package com.mypay.paymentqueries.adapters.kafka.events

import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentqueries.adapters.kafka.events.dto.AggregateID
import com.mypay.paymentqueries.adapters.kafka.events.dto.Money
import com.mypay.paymentqueries.adapters.kafka.events.dto.creditcard.CardHolder
import com.mypay.paymentqueries.adapters.kafka.events.dto.creditcard.CreditCard
import com.mypay.paymentqueries.adapters.kafka.events.dto.merchant.Merchant
import com.mypay.paymentqueries.adapters.kafka.events.dto.merchant.Order
import java.io.Serializable

class PaymentAuthorized(
    aggregateID: AggregateID,
    version: Int,
    val merchant: Merchant,
    val authorizationAmount: Money,
    val cardHolder: CardHolder,
    val creditCard: CreditCard,
    val order: Order
) : Serializable,
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "PaymentAuthorized(merchant=$merchant, authorizationAmount=$authorizationAmount, cardHolder=$cardHolder, creditCard=$creditCard, order=$order)"
    }
}
