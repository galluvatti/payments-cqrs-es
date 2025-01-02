package com.mypay.paymentgateway.infrastructure

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.infrastructure.EventStore
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.aggregates.payment.events.AuthorizedEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.CapturedEvent
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyViolation
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.ports.driver.AuthorizeCommand
import com.mypay.paymentgateway.domain.valueobjects.AnagraphicDetails
import com.mypay.paymentgateway.domain.valueobjects.Email
import com.mypay.paymentgateway.domain.valueobjects.Money
import com.mypay.paymentgateway.domain.valueobjects.Order
import com.mypay.paymentgateway.domain.valueobjects.address.Address
import com.mypay.paymentgateway.domain.valueobjects.address.City
import com.mypay.paymentgateway.domain.valueobjects.address.Country
import com.mypay.paymentgateway.domain.valueobjects.billing.BillingDetails
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import com.mypay.paymentgateway.domain.valueobjects.psp.AuthID
import com.mypay.paymentgateway.domain.valueobjects.psp.CaptureID
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class PaymentEventSourcingHandlerTest {
    private val authorizationAmount = Money(Currency.getInstance("EUR"), 100.0)
    private val captureAmount = Money(Currency.getInstance("EUR"), 50.0)
    private val cardHolder = CardHolder(
        AnagraphicDetails("John", "Doe"),
        BillingDetails(Country("IT"), City("Milan"), Address("Via di Casa Mia")),
        Email("itsme@mail.com")
    )
    private val creditCard =
        CreditCard("4111111111111111", "123", CreditCard.CardExpiration(1, 2030), CreditCard.CardType.VISA)
    private val order = Order("orderID", "Wonderful Hotel, reservation for 2 people, 3 nights from 01/01/2025")
    private val paymentProcessor = mockk<PaymentProcessor>()
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
            AuthorizedEvent(
                aggregateID,
                0,
                authorizationAmount,
                cardHolder,
                creditCard,
                order,
                AuthID("authID")
            ),
            CapturedEvent(
                aggregateID,
                1,
                captureAmount,
                CaptureID("captureID")
            )
        )
        val payment = PaymentEventSourcingHandler(eventStore).getById(aggregateID)
        assertThat(payment.version).isEqualTo(1)
        assertThat(payment.isCaptured()).isTrue()
        assertThat(payment.getAuthorizedAmount()).isEqualTo(authorizationAmount)
        assertThat(payment.getCapturedAmount()).isEqualTo(captureAmount)
    }

    private fun aPaymentWithAnAuthorizationEvent(): Payment {
        every { paymentProcessor.authorize(any(), any(), any()) } returns Ok(AuthID("authID"))
        val aggregateID = AggregateID(UUID.randomUUID())
        val aggregate = Payment(aggregateID)
        aggregate.authorize(
            AuthorizeCommand(
                aggregateID,
                authorizationAmount,
                cardHolder,
                creditCard,
                order,
                paymentProcessor
            )
        )
        return aggregate
    }
}