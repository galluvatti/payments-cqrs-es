package com.mypay.paymentqueries.adapters.kafka

import com.mypay.paymentqueries.adapters.kafka.events.*
import com.mypay.paymentqueries.adapters.kafka.events.dto.AggregateID
import com.mypay.paymentqueries.adapters.kafka.events.dto.Money
import com.mypay.paymentqueries.adapters.kafka.events.dto.address.Address
import com.mypay.paymentqueries.adapters.kafka.events.dto.address.City
import com.mypay.paymentqueries.adapters.kafka.events.dto.address.Country
import com.mypay.paymentqueries.adapters.kafka.events.dto.billing.BillingDetails
import com.mypay.paymentqueries.adapters.kafka.events.dto.billing.Email
import com.mypay.paymentqueries.adapters.kafka.events.dto.billing.FullName
import com.mypay.paymentqueries.adapters.kafka.events.dto.creditcard.CardHolder
import com.mypay.paymentqueries.adapters.kafka.events.dto.creditcard.CreditCard
import com.mypay.paymentqueries.adapters.kafka.events.dto.merchant.Merchant
import com.mypay.paymentqueries.adapters.kafka.events.dto.merchant.Order
import com.mypay.paymentqueries.domain.Payment
import com.mypay.paymentqueries.domain.PaymentRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.Acknowledgment
import java.time.LocalDateTime
import java.util.*

class PaymentEventHandlerTest {

    private val paymentRepository = mockk<PaymentRepository>()
    private val ack = mockk<Acknowledgment>()

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `consume a payment authorized event`() {
        every { paymentRepository.save(any()) } returns mockk<Payment>()
        every { ack.acknowledge() } returns Unit

        PaymentEventHandler(paymentRepository).consume(
            PaymentAuthorized(
                AggregateID(UUID.randomUUID()),
                1,
                Merchant("zalando"),
                Money(Currency.getInstance("EUR"), 100.0),
                CardHolder(
                    FullName("John", "Doe"),
                    BillingDetails(
                        Country("IT"),
                        City("Milan"),
                        Address("Via di casa mia 24")
                    ),
                    Email("johndoe@mail.com")
                ),
                CreditCard(
                    CreditCard.Pan("4111111111111111"),
                    "123",
                    CreditCard.CardExpiration(1, 2026),
                    CreditCard.CardBrand.AMEX
                ),
                Order("orderID", "A wonderful green T-Shirt")
            ),
            ack
        )
        verify { paymentRepository.save(any(Payment::class)) }
        verify { ack.acknowledge() }
    }

    @Test
    fun `consume a fraud detected event`() {
        every { paymentRepository.save(any()) } returns mockk<Payment>()
        every { ack.acknowledge() } returns Unit

        PaymentEventHandler(paymentRepository).consume(
            FraudDetected(
                AggregateID(UUID.randomUUID()),
                1,
                Merchant("zalando"),
                Money(Currency.getInstance("EUR"), 100.0),
                CardHolder(
                    FullName("John", "Doe"),
                    BillingDetails(
                        Country("IT"),
                        City("Milan"),
                        Address("Via di casa mia 24")
                    ),
                    Email("johndoe@mail.com")
                ),
                CreditCard(
                    CreditCard.Pan("4111111111111111"),
                    "123",
                    CreditCard.CardExpiration(1, 2026),
                    CreditCard.CardBrand.AMEX
                ),
                Order("orderID", "A wonderful green T-Shirt")
            ),
            ack
        )
        verify { paymentRepository.save(any(Payment::class)) }
        verify { ack.acknowledge() }
    }

    @Test
    fun `consume a payment captured event`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = mockk<Payment>()
        every { paymentRepository.findById(aggregateID.value.toString()) } returns Optional.of(payment)
        every { payment.addTransaction(any()) } returns Unit
        every { paymentRepository.save(any()) } returns payment
        every { ack.acknowledge() } returns Unit

        PaymentEventHandler(paymentRepository).consume(
            PaymentCaptured(
                aggregateID,
                1,
                Money(Currency.getInstance("EUR"), 100.0),
                10.0,
                LocalDateTime.now()
            ),
            ack
        )
        verify { paymentRepository.save(any(Payment::class)) }
        verify { ack.acknowledge() }
    }

    @Test
    fun `consume a payment capture failed event`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = mockk<Payment>()
        every { paymentRepository.findById(aggregateID.value.toString()) } returns Optional.of(payment)
        every { payment.addTransaction(any()) } returns Unit
        every { paymentRepository.save(any()) } returns payment
        every { ack.acknowledge() } returns Unit

        PaymentEventHandler(paymentRepository).consume(
            PaymentCaptureFailed(
                aggregateID,
                1,
                Money(Currency.getInstance("EUR"), 100.0)
            ),
            ack
        )
        verify { paymentRepository.save(any(Payment::class)) }
        verify { ack.acknowledge() }
    }

    @Test
    fun `consume a payment refunded event`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = mockk<Payment>()
        every { paymentRepository.findById(aggregateID.value.toString()) } returns Optional.of(payment)
        every { payment.addTransaction(any()) } returns Unit
        every { paymentRepository.save(any()) } returns payment
        every { ack.acknowledge() } returns Unit

        PaymentEventHandler(paymentRepository).consume(
            PaymentRefunded(
                aggregateID,
                1,
                Money(Currency.getInstance("EUR"), 100.0)
            ),
            ack
        )
        verify { paymentRepository.save(any(Payment::class)) }
        verify { ack.acknowledge() }
    }

    @Test
    fun `consume a payment refund failed event`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = mockk<Payment>()
        every { paymentRepository.findById(aggregateID.value.toString()) } returns Optional.of(payment)
        every { payment.addTransaction(any()) } returns Unit
        every { paymentRepository.save(any()) } returns payment
        every { ack.acknowledge() } returns Unit

        PaymentEventHandler(paymentRepository).consume(
            PaymentRefundFailed(
                aggregateID,
                1,
                Money(Currency.getInstance("EUR"), 100.0)
            ),
            ack
        )
        verify { paymentRepository.save(any(Payment::class)) }
        verify { ack.acknowledge() }
    }
}