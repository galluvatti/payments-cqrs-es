package com.mypay.paymentgateway.adapters.psp

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.mypay.paymentgateway.domain.errors.DomainError
import com.mypay.paymentgateway.domain.errors.InsufficientFunds
import com.mypay.paymentgateway.domain.errors.RefundWindowExpired
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID
import com.mypay.paymentgateway.domain.valueobjects.psp.RefundID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import kotlin.random.Random

@Service
class RandomResultPaymentProcessor : PaymentProcessor {
    private val logger = LoggerFactory.getLogger(RandomResultPaymentProcessor::class.java)
    override fun authorize(
        authorizationAmount: Money,
        cardHolder: CardHolder,
        creditCard: CreditCard
    ): Result<AuthID, DomainError> {
        logger.info("PSP is authorizing payment")
        return if (Random.nextBoolean()) {
            Ok(AuthID(UUID.randomUUID().toString()))
        } else Err(InsufficientFunds)
    }

    override fun capture(authorizationID: AuthID, captureAmount: Double): Result<CaptureID, DomainError> {
        logger.info("PSP is capturing payment with authID $authorizationID")
        return if (Random.nextBoolean()) {
            Ok(CaptureID(UUID.randomUUID().toString()))
        } else Err(InsufficientFunds)
    }

    override fun refund(captureID: CaptureID, captureAmount: Double): Result<RefundID, DomainError> {
        logger.info("PSP is refunding payment with captureID $captureID")
        return if (Random.nextBoolean()) {
            Ok(RefundID(UUID.randomUUID().toString()))
        } else Err(RefundWindowExpired)
    }
}