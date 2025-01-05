package com.mypay.paymentgateway.domain.services

import com.mypay.paymentgateway.domain.payment.creditcard.CardHolder
import com.mypay.paymentgateway.domain.payment.creditcard.CreditCard

interface FraudInvestigator {
    fun isFraud(cardHolder: CardHolder, creditCard: CreditCard): Boolean
}