package com.mypay.paymentgateway.adapters.persistence

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.events.EventModel
import com.mypay.cqrs.core.infrastructure.EventProducer
import com.mypay.paymentgateway.domain.events.Authorized
import com.mypay.paymentgateway.domain.events.Captured
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyViolation
import com.mypay.paymentgateway.domain.payment.Money
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

class PaymentEventStoreTest {
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

    private val repository = mockk<EventStoreRepository>()
    private val producer = mockk<EventProducer>()

    @Test
    fun `should retrieve events by aggregate id`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val authorizedEvent = Authorized(
            aggregateID, 0, Merchant("merchantID"), authorizationAmount, cardHolder, creditCard, order
        )
        val capturedEvent = Captured(aggregateID, 1, captureAmount, LocalDateTime.now())
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
        val authorizedEvent = Authorized(
            aggregateID, 0, Merchant("merchantID"), authorizationAmount, cardHolder, creditCard, order
        )
        val capturedEvent = Captured(aggregateID, 1, captureAmount, LocalDateTime.now())
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
        val authorizedEvent = Authorized(
            aggregateID, 0, Merchant("merchantID"), authorizationAmount, cardHolder, creditCard, order
        )
        val capturedEvent = Captured(aggregateID, 1, captureAmount, LocalDateTime.now())
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