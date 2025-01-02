package com.mypay.paymentgateway.domain.ports.driver

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.errors.InsufficientFunds
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyViolation
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class AuthorizeCommandHandlerTest {
    @Test
    fun `should authorize and save aggregate`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val paymentProcessor = mockk<PaymentProcessor>()

        every { paymentProcessor.authorize(any(), any(), any()) } returns Ok(AuthID("authID"))
        every { eventSourcingHandler.save(any()) } returns Ok(Unit)

        val authorizationResult = AuthorizeCommandHandler(eventSourcingHandler).handle(
            AuthorizeCommand(
                AggregateID(UUID.randomUUID()),
                Money(Currency.getInstance("EUR"), 100.00),
                CardHolder(
                    AnagraphicDetails("name", "surname"),
                    BillingDetails(
                        Country("IT"),
                        City("Milan"),
                        Address("Via di Casa mia")
                    ),
                    Email("itsme@mail.com")
                ),
                CreditCard(
                    "4111111111111111",
                    "123",
                    CreditCard.CardExpiration(1, 2025),
                    CreditCard.CardType.VISA
                ),
                Order("orderId", "Flight from MPX to LAX on 23/01/2025"),
                paymentProcessor
            )
        )

        assertThat(authorizationResult.isOk).isTrue()

        verify { paymentProcessor.authorize(any(), any(), any()) }
        verify { eventSourcingHandler.save(any()) }
    }

    @Test
    fun `should save aggregate even when authorization fails`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val paymentProcessor = mockk<PaymentProcessor>()
        val paymentProcessorResult = InsufficientFunds

        every { paymentProcessor.authorize(any(), any(), any()) } returns Err(paymentProcessorResult)
        every { eventSourcingHandler.save(any()) } returns Ok(Unit)

        val authorizationResult = AuthorizeCommandHandler(eventSourcingHandler).handle(
            AuthorizeCommand(
                AggregateID(UUID.randomUUID()),
                Money(Currency.getInstance("EUR"), 100.00),
                CardHolder(
                    AnagraphicDetails("name", "surname"),
                    BillingDetails(
                        Country("IT"),
                        City("Milan"),
                        Address("Via di Casa mia")
                    ),
                    Email("itsme@mail.com")
                ),
                CreditCard(
                    "4111111111111111",
                    "123",
                    CreditCard.CardExpiration(1, 2025),
                    CreditCard.CardType.VISA
                ),
                Order("orderId", "Flight from MPX to LAX on 23/01/2025"),
                paymentProcessor
            )
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.getError()).isEqualTo(paymentProcessorResult)

        verify { paymentProcessor.authorize(any(), any(), any()) }
        verify { eventSourcingHandler.save(any()) }
    }

    @Test
    fun `should return an error when aggregate saving fails`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val paymentProcessor = mockk<PaymentProcessor>()
        val eventSourcingHandlerResult = OptimisticConcurrencyViolation

        every { paymentProcessor.authorize(any(), any(), any()) } returns Ok(AuthID("authID"))
        every { eventSourcingHandler.save(any()) } returns Err(eventSourcingHandlerResult)

        val authorizationResult = AuthorizeCommandHandler(eventSourcingHandler).handle(
            AuthorizeCommand(
                AggregateID(UUID.randomUUID()),
                Money(Currency.getInstance("EUR"), 100.00),
                CardHolder(
                    AnagraphicDetails("name", "surname"),
                    BillingDetails(
                        Country("IT"),
                        City("Milan"),
                        Address("Via di Casa mia")
                    ),
                    Email("itsme@mail.com")
                ),
                CreditCard(
                    "4111111111111111",
                    "123",
                    CreditCard.CardExpiration(1, 2025),
                    CreditCard.CardType.VISA
                ),
                Order("orderId", "Flight from MPX to LAX on 23/01/2025"),
                paymentProcessor
            )
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.getError()).isEqualTo(eventSourcingHandlerResult)

        verify { paymentProcessor.authorize(any(), any(), any()) }
        verify { eventSourcingHandler.save(any()) }
    }

}