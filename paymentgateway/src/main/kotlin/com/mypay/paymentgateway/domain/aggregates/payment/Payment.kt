package com.mypay.paymentgateway.domain.aggregates.payment

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.aggregates.AggregateRoot
import com.mypay.paymentgateway.domain.aggregates.payment.events.*
import com.mypay.paymentgateway.domain.errors.*
import com.mypay.paymentgateway.domain.ports.driver.AuthorizeCommand
import com.mypay.paymentgateway.domain.ports.driver.CaptureCommand
import com.mypay.paymentgateway.domain.ports.driver.RefundCommand
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID
import com.mypay.paymentgateway.domain.valueobjects.psp.RefundID
import org.slf4j.LoggerFactory

class Payment(id: AggregateID) : AggregateRoot(id) {
    private val logger = LoggerFactory.getLogger(Payment::class.java)
    private var isAuthorized = false
    private var isCaptured = false
    private var isRefunded = false
    private lateinit var authorizedAmount: Money
    private lateinit var capturedAmount: Money
    private lateinit var refundedAmount: Money
    private lateinit var authID: AuthID
    private lateinit var captureID: CaptureID
    private lateinit var refundID: RefundID

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
        if (command.captureAmount > this.authorizedAmount.amount) return Err(InsufficientFunds)
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

    fun refund(command: RefundCommand): Result<Unit, DomainError> {
        if (!isCaptured || isRefunded) return Err(RefundNotAllowed)
        if (command.refundAmount > this.capturedAmount.amount) return Err(RefundNotAllowed)
        val pspResponse = command.paymentProcessor.refund(captureID, command.refundAmount)
            .onSuccess {
                raiseEvent(
                    RefundedEvent(
                        this.id,
                        this.version,
                        Money(this.authorizedAmount.currency, command.refundAmount),
                        it
                    )
                )
            }
            .onFailure {
                logger.error("Failed refund for Payment with details $command. Reason: $it")
                raiseEvent(
                    RefundFailedEvent(
                        this.id,
                        this.version,
                        Money(this.authorizedAmount.currency, command.refundAmount),
                        it.description
                    )
                )
            }

        return pspResponse.mapBoth(
            { Ok(Unit) },
            { Err(it) }
        )
    }

    private fun apply(event: AuthorizedEvent) {
        this.isAuthorized = true
        this.authID = event.authID
        this.authorizedAmount = event.authorizationAmount
    }

    private fun apply(event: AuthorizationFailedEvent) {
    }

    private fun apply(event: CapturedEvent) {
        this.captureID = event.captureID
        this.capturedAmount = event.captureAmount
        this.isCaptured = true
    }

    private fun apply(event: CaptureFailedEvent) {
    }

    private fun apply(event: RefundedEvent) {
        this.refundID = event.refundID
        this.refundedAmount = event.refundAmount
        this.isRefunded = true
    }

    private fun apply(event: RefundFailedEvent) {
    }

    fun isAuthorized(): Boolean {
        return this.isAuthorized
    }

    fun isCaptured(): Boolean {
        return this.isCaptured
    }

    fun isRefunded(): Boolean {
        return this.isRefunded
    }

    fun getAuthorizedAmount(): Money {
        return this.authorizedAmount
    }

    fun getCapturedAmount(): Money {
        return this.capturedAmount
    }

    fun getRefundedAmount(): Money {
        return this.refundedAmount
    }

}