package com.mypay.paymentgateway.adapters.psp

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.mypay.paymentgateway.domain.errors.PaymentGatewayError
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID
import org.springframework.stereotype.Service
import java.util.*

@Service
class AlwaysOKPaymentProcessor : PaymentProcessor {
    override fun authorize(
        authorizationAmount: Money,
        cardHolder: CardHolder,
        creditCard: CreditCard
    ): Result<AuthID, PaymentGatewayError> {
        return Ok(AuthID(UUID.randomUUID().toString()))
    }

    override fun capture(captureAmount: Money): Result<CaptureID, PaymentGatewayError> {
        return Ok(CaptureID(UUID.randomUUID().toString()))
    }
}