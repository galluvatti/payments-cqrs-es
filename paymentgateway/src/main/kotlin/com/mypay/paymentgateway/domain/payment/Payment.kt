package com.mypay.paymentgateway.domain.payment

import com.github.michaelbull.result.*
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.aggregates.AggregateRoot
import com.mypay.paymentgateway.domain.errors.*
import com.mypay.paymentgateway.domain.events.*
import com.mypay.paymentgateway.domain.services.FraudInvestigator
import com.mypay.paymentgateway.domain.valueobjects.Merchant
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.Order
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import org.slf4j.LoggerFactory

class Payment(id: AggregateID) : AggregateRoot(id) {
    private val logger = LoggerFactory.getLogger(Payment::class.java)
    private lateinit var authorizedAmount: Money
    private lateinit var capturedAmount: Money
    private lateinit var refundedAmount: Money
    private var status: Status = Status.INITIATED

    enum class Status {
        INITIATED, FRAUD, AUTHORIZED, CAPTURED, REFUNDED
    }

    fun authorize(
        merchant: Merchant,
        amount: Money,
        cardHolder: CardHolder,
        creditCard: CreditCard,
        order: Order,
        fraudInvestigator: FraudInvestigator
    ): Result<Unit, DomainError> {
        if(status != Status.INITIATED) return Err(PaymentAlreadyAuthorized)
        val fraudDetected = fraudInvestigator.isFraud(cardHolder, creditCard)
        return if (fraudDetected) {
            raiseEvent(
                FraudDetected(
                    this.id,
                    this.version,
                    merchant,
                    amount,
                    cardHolder,
                    creditCard,
                    order,
                )
            )
            Err(SuspectFraud)
        } else {
            raiseEvent(
                Authorized(
                    this.id,
                    this.version,
                    merchant,
                    amount,
                    cardHolder,
                    creditCard,
                    order,
                )
            )
            Ok(Unit)
        }
    }

    fun capture(amount: Double): Result<Unit, DomainError> {
        if (status != Status.AUTHORIZED) return Err(CaptureNotAllowed)
        return if (amount > this.authorizedAmount.amount) {
            logger.error("Capture not allowed for payment $id: the requested amount is greater than authorized amount.")
            raiseEvent(
                CaptureFailed(
                    this.id,
                    this.version,
                    Money(this.authorizedAmount.currency, amount),
                )
            )
            Err(InsufficientFunds)
        } else {
            raiseEvent(
                Captured(
                    this.id,
                    this.version,
                    Money(this.authorizedAmount.currency, amount),
                )
            )
            Ok(Unit)
        }


    }

    fun refund(amount: Double): Result<Unit, DomainError> {
        if (status != Status.CAPTURED) return Err(RefundNotAllowed)
        return if (amount > this.capturedAmount.amount) {
            logger.error("Refund not allowed for payment $id: the requested amount is greater than captured amount.")
            raiseEvent(
                RefundFailed(
                    this.id,
                    this.version,
                    Money(this.authorizedAmount.currency, amount),
                )
            )
            Err(RefundNotAllowed)
        } else {
            raiseEvent(
                Refunded(
                    this.id,
                    this.version,
                    Money(this.authorizedAmount.currency, amount),
                )
            )
            Ok(Unit)
        }
    }

    private fun apply(event: FraudDetected) {
        this.status = Status.FRAUD
    }

    private fun apply(event: Authorized) {
        this.authorizedAmount = event.authorizationAmount
        this.status = Status.AUTHORIZED
    }


    private fun apply(event: Captured) {
        this.capturedAmount = event.captureAmount
        this.status = Status.CAPTURED
    }

    private fun apply(event: CaptureFailed) {
        logger.info("Doing nothing for this event $event")
    }

    private fun apply(event: Refunded) {
        this.refundedAmount = event.refundAmount
        this.status = Status.REFUNDED
    }

    private fun apply(event: RefundFailed) {
        logger.info("Doing nothing for this event $event")
    }

    fun getStatus() = status

}