package com.mypay.paymentgateway.domain.aggregates.payment.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.Order
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID

class AuthorizedEvent(
    aggregateID: AggregateID,
    version: Int,
    val authorizationAmount: Money,
    val cardHolder: CardHolder,
    val creditCard: CreditCard,
    val order: Order,
    val authID: AuthID
) :
    DomainEvent(aggregateID, version) {
    override fun toString(): String {
        return "AuthorizedEvent(authorizationAmount=$authorizationAmount, cardHolder=$cardHolder, creditCard=$creditCard, order=$order, authID=$authID)"
    }
}
