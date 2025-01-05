package com.mypay.paymentgateway.domain.services

import com.mypay.paymentgateway.domain.payment.billing.Email
import com.mypay.paymentgateway.domain.payment.creditcard.CardHolder
import com.mypay.paymentgateway.domain.payment.creditcard.CreditCard
import org.springframework.stereotype.Service

@Service
class InMemoryBlacklistFraudInvestigator(
    private val creditCardsBlacklist: List<CreditCard.Pan>,
    private val emailBlacklist: List<Email>
) : FraudInvestigator {
    override fun isFraud(cardHolder: CardHolder, creditCard: CreditCard): Boolean {
        return creditCardsBlacklist.contains(creditCard.pan) || emailBlacklist.contains(cardHolder.email)
    }
}