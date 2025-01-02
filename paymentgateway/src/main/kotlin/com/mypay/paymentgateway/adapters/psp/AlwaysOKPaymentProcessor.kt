package com.mypay.paymentgateway.adapters.psp

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.mypay.paymentgateway.domain.errors.DomainError
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class AlwaysOKPaymentProcessor : PaymentProcessor {
    private val logger = LoggerFactory.getLogger(AlwaysOKPaymentProcessor::class.java)
    override fun authorize(
        authorizationAmount: Money,
        cardHolder: CardHolder,
        creditCard: CreditCard
    ): Result<AuthID, DomainError> {
        logger.info("PSP is authorizing payment")
        return Ok(AuthID(UUID.randomUUID().toString()))
    }

    override fun capture(authorizationID: AuthID, captureAmount: Double): Result<CaptureID, DomainError> {
        logger.info("PSP is capturing payment")
        return Ok(CaptureID(UUID.randomUUID().toString()))
    }
}