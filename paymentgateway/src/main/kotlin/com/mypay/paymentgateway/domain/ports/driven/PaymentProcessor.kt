package com.mypay.paymentgateway.domain.ports.driven

import com.github.michaelbull.result.Result
import com.mypay.paymentgateway.domain.errors.PaymentGatewayError
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID

interface PaymentProcessor {
    fun authorize(
        authorizationAmount: Money,
        cardHolder: CardHolder,
        creditCard: CreditCard
    ): Result<AuthID, PaymentGatewayError>

    fun capture(captureAmount: Money): Result<CaptureID, PaymentGatewayError>
}