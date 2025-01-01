package com.mypay.paymentgateway.domain.aggregates.payment.events

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.Order
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard

data class AuthorizationFailedEvent(
    val aggregateID: AggregateID,
    val version: Int,
    val authorizationAmount: Money,
    val cardHolder: CardHolder,
    val creditCard: CreditCard,
    val order: Order,
    val failureReason: String
) :
    DomainEvent(aggregateID, version)