package com.mypay.paymentgateway.domain.aggregates.payment

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.aggregates.AggregateRoot
import com.mypay.paymentgateway.domain.aggregates.payment.events.AuthorizedPaymentEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.CapturedPaymentEvent
import com.mypay.paymentgateway.domain.errors.PaymentAlreadyAuthorized
import com.mypay.paymentgateway.domain.errors.PaymentGatewayError
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.ports.driver.AuthorizeCommand
import com.mypay.paymentgateway.domain.ports.driver.CaptureCommand
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID
import org.slf4j.LoggerFactory

class Payment(id: AggregateID, private val paymentProcessor: PaymentProcessor) : AggregateRoot(id) {
    private val logger = LoggerFactory.getLogger(Payment::class.java)
    private var isAuthorized = false

    private var isCaptured = false
    private lateinit var authorizedAmount: Money
    private lateinit var authID: AuthID
    private lateinit var capturedAmount: Money
    private lateinit var captureID: CaptureID

    fun authorize(command: AuthorizeCommand): Result<Unit, PaymentGatewayError> {
        if (isAuthorized) return Err(PaymentAlreadyAuthorized)

        val pspResponse = paymentProcessor.authorize(
            command.authorizationAmount,
            command.cardHolder,
            command.creditCard
        )
            .onSuccess {
                raiseEvent(
                    AuthorizedPaymentEvent(
                        this.id,
                        this.version,
                        command.authorizationAmount,
                        command.cardHolder,
                        command.creditCard,
                        command.order,
                        it
                    )
                )
            }
            .onFailure {
                //TODO Raise events
                    err ->
                logger.error("Failed authorization for Payment with details $command. Reason: $err")
            }

        return pspResponse.mapBoth(
            { _ -> Ok(Unit) },
            { err -> Err(err) }
        )
    }

    fun capture(command: CaptureCommand): Result<Unit, PaymentGatewayError> {
        val pspResponse = paymentProcessor.capture(command.captureAmount)
            .onSuccess {
                raiseEvent(
                    CapturedPaymentEvent(
                        this.id,
                        this.version,
                        command.captureAmount,
                        it
                    )
                )
            }
            .onFailure {
                //TODO Raise event
                    err ->
                logger.error("Failed capture for Payment with details $command. Reason: $err")
            }

        return pspResponse.mapBoth(
            { _ -> Ok(Unit) },
            { err -> Err(err) }
        )
    }

    private fun apply(event: AuthorizedPaymentEvent) {
        this.isAuthorized = true
        this.authID = event.authID
        this.authorizedAmount = event.authorizationAmount
    }

    private fun apply(event: CapturedPaymentEvent) {
        this.isCaptured = true
        this.captureID = event.captureID
        this.capturedAmount = event.capturedAmount
    }

    fun isAuthorized(): Boolean {
        return this.isAuthorized
    }

    fun isCaptured(): Boolean {
        return this.isCaptured
    }

    fun getAuthorizedAmount(): Money {
        return this.authorizedAmount
    }

    fun getCapturedAmount(): Money {
        return this.capturedAmount
    }

}