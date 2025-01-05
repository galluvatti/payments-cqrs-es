package com.mypay.paymentgateway.domain.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.payment.Money
import com.mypay.paymentgateway.domain.payment.merchant.Order
import com.mypay.paymentgateway.domain.payment.creditcard.CardHolder
import com.mypay.paymentgateway.domain.payment.creditcard.CreditCard

class AuthorizationFailed(
    aggregateID: AggregateID,
    version: Int,
    val authorizationAmount: Money,
    val cardHolder: CardHolder,
    val creditCard: CreditCard,
    val order: Order,
    val failureReason: String
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "AuthorizationFailedEvent(authorizationAmount=$authorizationAmount, cardHolder=$cardHolder, creditCard=$creditCard, order=$order, failureReason='$failureReason')"
    }
}
