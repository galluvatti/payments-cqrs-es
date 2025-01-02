package com.mypay.paymentgateway.adapters.persistence

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.EventModel
import com.mypay.cqrs.core.infrastructure.EventProducer
import com.mypay.paymentgateway.domain.aggregates.payment.events.AuthorizedEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.CapturedEvent
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyViolation
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

class PaymentEventStoreTest {
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

    private val repository = mockk<EventStoreRepository>()
    private val producer = mockk<EventProducer>()

    @Test
    fun `should retrieve events by aggregate id`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val authorizedEvent = AuthorizedEvent(
            aggregateID, 0, authorizationAmount, cardHolder, creditCard, order, AuthID("authID")
        )
        val capturedEvent = CapturedEvent(aggregateID, 1, captureAmount, CaptureID("captureID"))
        every { repository.findByAggregateId(aggregateID.value.toString()) } returns listOf(
            EventModel(
                "id1", Date(), aggregateID.toString(), "Payment", 0,
                "AuthorizedEvent",
                authorizedEvent,
            ),
            EventModel(
                "id1", Date(), aggregateID.toString(), "Payment", 1, "CapturedEvent",
                capturedEvent
            )
        )

        val events = PaymentEventStore(repository, producer).getEvents(aggregateID)
        assertThat(events).containsExactly(authorizedEvent, capturedEvent)
    }

    @Test
    fun `should save events by aggregate id and publish them`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val authorizedEvent = AuthorizedEvent(
            aggregateID, 0, authorizationAmount, cardHolder, creditCard, order, AuthID("authID")
        )
        val capturedEvent = CapturedEvent(aggregateID, 1, captureAmount, CaptureID("captureID"))
        every { repository.findByAggregateId(aggregateID.value.toString()) } returns listOf(
            EventModel(
                "id1", Date(), aggregateID.toString(), "Payment", 0,
                "AuthorizedEvent",
                authorizedEvent,
            )
        )
        every { repository.save(any()) } returns mockk<EventModel>()
        every { producer.produce(capturedEvent) } returns Unit

        val result = PaymentEventStore(repository, producer).saveEvents(aggregateID, listOf(capturedEvent), 0)
        assertThat(result.isOk).isTrue()
        verify { repository.save(any()) }
        verify { producer.produce(capturedEvent) }
    }

    @Test
    fun `should return an error when saving events on a wrong aggregate version`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val authorizedEvent = AuthorizedEvent(
            aggregateID, 0, authorizationAmount, cardHolder, creditCard, order, AuthID("authID")
        )
        val capturedEvent = CapturedEvent(aggregateID, 1, captureAmount, CaptureID("captureID"))
        every { repository.findByAggregateId(aggregateID.value.toString()) } returns listOf(
            EventModel(
                "id1", Date(), aggregateID.toString(), "Payment", 0,
                "AuthorizedEvent",
                authorizedEvent,
            ),
            EventModel(
                "id2", Date(), aggregateID.toString(), "Payment", 1,
                "CapturedEvent",
                capturedEvent
            )
        )
        every { repository.save(any()) } returns mockk<EventModel>()

        val result = PaymentEventStore(repository, producer).saveEvents(aggregateID, listOf(capturedEvent), 0)

        assertThat(result.isErr).isTrue()
        assertThat(result.error).isEqualTo(OptimisticConcurrencyViolation)
        verify(exactly = 0){ repository.save(any()) }
        verify(exactly = 0) { producer.produce(capturedEvent) }
    }
}