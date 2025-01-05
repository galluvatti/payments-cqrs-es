package com.mypay.paymentgateway.application.handlers

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.application.commands.Authorize
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyViolation
import com.mypay.paymentgateway.domain.payment.Money
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.services.InMemoryBlacklistFraudInvestigator
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
import java.util.*

class AuthorizeHandlerTest {
    @Test
    fun `should authorize and save aggregate`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        every { eventSourcingHandler.save(any()) } returns Ok(Unit)

        val authorizationResult = AuthorizeHandler(
            eventSourcingHandler, InMemoryBlacklistFraudInvestigator(
                emptyList(),
                emptyList()
            )
        ).handle(
            Authorize(
                Merchant("merchantID"),
                AggregateID(UUID.randomUUID()),
                Money(Currency.getInstance("EUR"), 100.00),
                CardHolder(
                    FullName("name", "surname"),
                    BillingDetails(
                        Country("IT"),
                        City("Milan"),
                        Address("Via di Casa mia")
                    ),
                    Email("itsme@mail.com")
                ),
                CreditCard(
                    CreditCard.Pan("4111111111111111"),
                    "123",
                    CreditCard.CardExpiration(1, 2025),
                    CreditCard.CardBrand.VISA
                ),
                Order("orderId", "Flight from MPX to LAX on 23/01/2025")
            )
        )

        assertThat(authorizationResult.isOk).isTrue()

        verify { eventSourcingHandler.save(any(Payment::class)) }
    }

    @Test
    fun `should save aggregate even when authorization fails`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        every { eventSourcingHandler.save(any()) } returns Ok(Unit)

        val authorizationResult = AuthorizeHandler(
            eventSourcingHandler,
            InMemoryBlacklistFraudInvestigator(
                listOf(CreditCard.Pan("4111111111111111")),
                emptyList()
            )
        ).handle(
            Authorize(
                Merchant("merchantID"),
                AggregateID(UUID.randomUUID()),
                Money(Currency.getInstance("EUR"), 100.00),
                CardHolder(
                    FullName("name", "surname"),
                    BillingDetails(
                        Country("IT"),
                        City("Milan"),
                        Address("Via di Casa mia")
                    ),
                    Email("itsme@mail.com")
                ),
                CreditCard(
                    CreditCard.Pan("4111111111111111"),
                    "123",
                    CreditCard.CardExpiration(1, 2025),
                    CreditCard.CardBrand.VISA
                ),
                Order("orderId", "Flight from MPX to LAX on 23/01/2025")
            )
        )

        assertThat(authorizationResult.isErr).isTrue()
    }

    @Test
    fun `should return an error when aggregate saving fails`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val eventSourcingHandlerResult = OptimisticConcurrencyViolation

        every { eventSourcingHandler.save(any()) } returns Err(eventSourcingHandlerResult)

        val authorizationResult = AuthorizeHandler(
            eventSourcingHandler, InMemoryBlacklistFraudInvestigator(
                emptyList(),
                emptyList()
            )
        ).handle(
            Authorize(
                Merchant("merchantID"),
                AggregateID(UUID.randomUUID()),
                Money(Currency.getInstance("EUR"), 100.00),
                CardHolder(
                    FullName("name", "surname"),
                    BillingDetails(
                        Country("IT"),
                        City("Milan"),
                        Address("Via di Casa mia")
                    ),
                    Email("itsme@mail.com")
                ),
                CreditCard(
                    CreditCard.Pan("4111111111111111"),
                    "123",
                    CreditCard.CardExpiration(1, 2025),
                    CreditCard.CardBrand.VISA
                ),
                Order("orderId", "Flight from MPX to LAX on 23/01/2025")
            )
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.getError()).isEqualTo(eventSourcingHandlerResult)
    }

}