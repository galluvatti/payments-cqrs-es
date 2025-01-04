package com.mypay.paymentgateway.domain.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.Order
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard

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
