package com.mypay.paymentgateway.domain.services

import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard

interface FraudInvestigator {
    fun isFraud(cardHolder: CardHolder, creditCard: CreditCard): Boolean
}