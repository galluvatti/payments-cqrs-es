package com.mypay.paymentgateway.adapters.persistence

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.infrastructure.EventStore
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyViolation
import com.mypay.paymentgateway.domain.events.PaymentAuthorized
import com.mypay.paymentgateway.domain.events.PaymentCaptured
import com.mypay.paymentgateway.domain.payment.Money
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.services.FraudInvestigator
import com.mypay.paymentgateway.domain.payment.address.Address
import com.mypay.paymentgateway.domain.payment.address.City
import com.mypay.paymentgateway.domain.payment.address.Country
import com.mypay.paymentgateway.domain.payment.billing.BillingDetails
import com.mypay.paymentgateway.domain.payment.billing.Email
import com.mypay.paymentgateway.domain.payment.billing.FullName
import com.mypay.paymentgateway.domain.payment.creditcard.CardHolder
import com.mypay.paymentgateway.domain.payment.creditcard.CreditCard
import com.mypay.paymentgateway.domain.payment.merchant.Merchant
import com.mypay.paymentgateway.domain.payment.merchant.Order
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class PaymentEventSourcingHandlerTest {
    private val authorizationAmount = Money(Currency.getInstance("EUR"), 100.0)
    private val captureAmount = Money(Currency.getInstance("EUR"), 50.0)
    private val cardHolder = CardHolder(
        FullName("John", "Doe"),
        BillingDetails(Country("IT"), City("Milan"), Address("Via di Casa Mia")),
        Email("itsme@mail.com")
    )
    private val creditCard =
        CreditCard(
            CreditCard.Pan("4111111111111111"),
            "123",
            CreditCard.CardExpiration(1, 2030),
            CreditCard.CardBrand.VISA
        )
    private val order = Order("orderID", "Wonderful Hotel, reservation for 2 people, 3 nights from 01/01/2025")
    private val fraudInvestigator = mockk<FraudInvestigator>()
    private val eventStore = mockk<EventStore>()

    @Test
    fun `given an aggregate with 1 uncommitted change, it should persist the change and mark it as committed`() {
        val aggregate = aPaymentWithAnAuthorizationEvent()

        every { eventStore.saveEvents(any(), any(), any()) } returns Ok(Unit)

        val result = PaymentEventSourcingHandler(eventStore).save(aggregate)

        assertThat(result.isOk).isTrue()
        assertThat(aggregate.getUncommitedChanges()).hasSize(0)
        verify { eventStore.saveEvents(any(), any(), any()) }
    }

    @Test
    fun `given an aggregate with 1 uncommitted change, it should return an error when event persistence fails`() {
        val aggregate = aPaymentWithAnAuthorizationEvent()

        every { eventStore.saveEvents(any(), any(), any()) } returns Err(OptimisticConcurrencyViolation)

        val result = PaymentEventSourcingHandler(eventStore).save(aggregate)

        assertThat(result.isErr).isTrue()
        assertThat(aggregate.getUncommitedChanges()).hasSize(1)
    }

    @Test
    fun `given an aggregate ID, it should load load past events and replay them`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        every { eventStore.getEvents(aggregateID) } returns listOf(
            PaymentAuthorized(
                aggregateID,
                0,
                Merchant("merchantID"),
                authorizationAmount,
                cardHolder,
                creditCard,
                order
            ),
            PaymentCaptured(
                aggregateID,
                1,
                captureAmount,
                1.00,
                LocalDateTime.now()
            )
        )
        val payment = PaymentEventSourcingHandler(eventStore).getById(aggregateID)
        assertThat(payment.version).isEqualTo(1)
        assertThat(payment.getStatus()).isEqualTo(Payment.Status.CAPTURED)
    }

    private fun aPaymentWithAnAuthorizationEvent(): Payment {
        every { fraudInvestigator.isFraud(any(), any()) } returns false
        val aggregateID = AggregateID(UUID.randomUUID())
        val aggregate = Payment(aggregateID)
        aggregate.authorize(
            Merchant("merchantID"),
            authorizationAmount,
            cardHolder,
            creditCard,
            order,
            fraudInvestigator
        )
        return aggregate
    }
}