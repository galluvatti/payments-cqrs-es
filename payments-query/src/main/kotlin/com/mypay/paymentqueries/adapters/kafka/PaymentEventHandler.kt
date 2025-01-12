package com.mypay.paymentqueries.adapters.kafka

import com.mypay.paymentqueries.adapters.kafka.events.*
import com.mypay.paymentqueries.domain.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class PaymentEventHandler(
    @Autowired private val paymentRepository: PaymentRepository
) {
    fun consume(event: PaymentAuthorized, ack: Acknowledgment) {
        val payment = Payment(
            event.aggregateID.value.toString(),
            CreditCard(
                event.creditCard.pan.value,
                event.creditCard.cvv,
                event.creditCard.expiration.month,
                event.creditCard.expiration.year,
                Brand.valueOf(event.creditCard.type.toString()),
                "${event.cardHolder.fullName.name} ${event.cardHolder.fullName.surname}",
                event.cardHolder.email.value,
                Address(
                    event.cardHolder.billingDetails.country.isoCode,
                    event.cardHolder.billingDetails.city.value,
                    event.cardHolder.billingDetails.address.value,
                )
            ),
            event.merchant.id,
            event.order.id,
            event.order.description,
            mutableListOf(
                Transaction(
                    type = TransactionType.AUTHORIZE,
                    amount = event.authorizationAmount.amount,
                    currency = event.authorizationAmount.currency.currencyCode,
                    result = TransactionResult.OK
                )
            )
        )
        paymentRepository.save(payment)
        ack.acknowledge()
    }

    fun consume(event: FraudDetected, ack: Acknowledgment) {
        val payment = Payment(
            event.aggregateID.value.toString(),
            CreditCard(
                event.creditCard.pan.value,
                event.creditCard.cvv,
                event.creditCard.expiration.month,
                event.creditCard.expiration.year,
                Brand.valueOf(event.creditCard.type.toString()),
                "${event.cardHolder.fullName.name} ${event.cardHolder.fullName.surname}",
                event.cardHolder.email.value,
                Address(
                    event.cardHolder.billingDetails.country.isoCode,
                    event.cardHolder.billingDetails.city.value,
                    event.cardHolder.billingDetails.address.value,
                )
            ),
            event.merchant.id,
            event.order.id,
            event.order.description,
            mutableListOf(
                Transaction(
                    type = TransactionType.AUTHORIZE,
                    amount = event.authorizationAmount.amount,
                    currency = event.authorizationAmount.currency.currencyCode,
                    result = TransactionResult.FRAUD
                )
            )
        )
        paymentRepository.save(payment)
        ack.acknowledge()
    }

    fun consume(event: PaymentCaptured, ack: Acknowledgment) {
        paymentRepository.findById(event.aggregateID.value.toString()).ifPresent {
            it.addTransaction(
                Transaction(
                    type = TransactionType.CAPTURE,
                    amount = event.captureAmount.amount,
                    currency = event.captureAmount.currency.currencyCode,
                    result = TransactionResult.OK,
                    fees = event.fees,
                    timestamp = event.captureDate
                )
            )
            paymentRepository.save(it)
        }
        ack.acknowledge()
    }

    fun consume(event: PaymentCaptureFailed, ack: Acknowledgment) {
        paymentRepository.findById(event.aggregateID.value.toString()).ifPresent {
            it.addTransaction(
                Transaction(
                    type = TransactionType.CAPTURE,
                    amount = event.captureAmount.amount,
                    currency = event.captureAmount.currency.currencyCode,
                    result = TransactionResult.KO
                )
            )
            paymentRepository.save(it)
        }
        ack.acknowledge()
    }

    fun consume(event: PaymentRefunded, ack: Acknowledgment) {
        paymentRepository.findById(event.aggregateID.value.toString()).ifPresent {
            it.addTransaction(
                Transaction(
                    type = TransactionType.REFUND,
                    amount = event.refundAmount.amount,
                    currency = event.refundAmount.currency.currencyCode,
                    result = TransactionResult.OK
                )
            )
            paymentRepository.save(it)
        }
        ack.acknowledge()
    }

    fun consume(event: PaymentRefundFailed, ack: Acknowledgment) {
        paymentRepository.findById(event.aggregateID.value.toString()).ifPresent {
            it.addTransaction(
                Transaction(
                    type = TransactionType.REFUND,
                    amount = event.refundAmount.amount,
                    currency = event.refundAmount.currency.currencyCode,
                    result = TransactionResult.KO
                )
            )
            paymentRepository.save(it)
        }
        ack.acknowledge()
    }
}