package com.mypay.paymentgateway.domain.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.payment.merchant.Merchant
import com.mypay.paymentgateway.domain.payment.Money
import com.mypay.paymentgateway.domain.payment.merchant.Order
import com.mypay.paymentgateway.domain.payment.creditcard.CardHolder
import com.mypay.paymentgateway.domain.payment.creditcard.CreditCard

class FraudDetected(
    aggregateID: AggregateID,
    version: Int,
    val merchant: Merchant,
    val authorizationAmount: Money,
    val cardHolder: CardHolder,
    val creditCard: CreditCard,
    val order: Order
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "FraudDetected(authorizationAmount=$authorizationAmount, cardHolder=$cardHolder, creditCard=$creditCard, order=$order)"
    }
}
