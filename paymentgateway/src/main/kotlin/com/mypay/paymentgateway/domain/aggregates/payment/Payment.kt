package com.mypay.paymentgateway.domain.aggregates.payment

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.aggregates.AggregateRoot
import com.mypay.paymentgateway.domain.aggregates.payment.events.AuthorizationFailedEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.AuthorizedEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.CaptureFailedEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.CapturedEvent
import com.mypay.paymentgateway.domain.errors.CaptureNotAllowed
import com.mypay.paymentgateway.domain.errors.DomainError
import com.mypay.paymentgateway.domain.errors.InsufficientFunds
import com.mypay.paymentgateway.domain.errors.PaymentAlreadyAuthorized
import com.mypay.paymentgateway.domain.ports.driver.AuthorizeCommand
import com.mypay.paymentgateway.domain.ports.driver.CaptureCommand
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID
import org.slf4j.LoggerFactory

class Payment(id: AggregateID) : AggregateRoot(id) {
    private val logger = LoggerFactory.getLogger(Payment::class.java)
    private var isAuthorized = false

    private var isCaptured = false
    private lateinit var authorizedAmount: Money
    private lateinit var authID: AuthID
    private lateinit var capturedAmount: Money
    private val captureIDs = mutableListOf<CaptureID>()

    fun authorize(command: AuthorizeCommand): Result<Unit, DomainError> {
        if (isAuthorized) return Err(PaymentAlreadyAuthorized)

        val pspResponse = command.paymentProcessor.authorize(
            command.authorizationAmount,
            command.cardHolder,
            command.creditCard
        )
            .onSuccess {
                raiseEvent(
                    AuthorizedEvent(
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
                logger.error("Failed authorization for Payment with details $command. Reason: $it")
                raiseEvent(
                    AuthorizationFailedEvent(
                        this.id,
                        this.version,
                        command.authorizationAmount,
                        command.cardHolder,
                        command.creditCard,
                        command.order,
                        it.description
                    )
                )

            }

        return pspResponse.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }

    fun capture(command: CaptureCommand): Result<Unit, DomainError> {
        if (!isAuthorized) return Err(CaptureNotAllowed)
        if (exceedsCapturableAmount(command.captureAmount)) return Err(InsufficientFunds)
        val pspResponse = command.paymentProcessor.capture(authID, command.captureAmount)
            .onSuccess {
                raiseEvent(
                    CapturedEvent(
                        this.id,
                        this.version,
                        Money(this.authorizedAmount.currency, command.captureAmount),
                        it
                    )
                )
            }
            .onFailure {
                logger.error("Failed capture for Payment with details $command. Reason: $it")
                raiseEvent(
                    CaptureFailedEvent(
                        this.id,
                        this.version,
                        Money(this.authorizedAmount.currency, command.captureAmount),
                        it.description
                    )
                )
            }

        return pspResponse.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }

    private fun exceedsCapturableAmount(requestedAmount: Double): Boolean {
        return if (!isCaptured()) {
            requestedAmount > this.authorizedAmount.amount
        } else {
            requestedAmount > this.authorizedAmount.amount - capturedAmount.amount
        }
    }

    private fun apply(event: AuthorizedEvent) {
        this.isAuthorized = true
        this.authID = event.authID
        this.authorizedAmount = event.authorizationAmount
    }

    private fun apply(event: AuthorizationFailedEvent) {
    }

    private fun apply(event: CapturedEvent) {
        this.captureIDs.add(event.captureID)
        if (!isCaptured()) {
            this.capturedAmount = event.captureAmount
        } else {
            this.capturedAmount =
                Money(event.captureAmount.currency, this.capturedAmount.amount + event.captureAmount.amount)
        }
        this.isCaptured = true
    }

    private fun apply(event: CaptureFailedEvent) {
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