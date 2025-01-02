package com.mypay.paymentgateway.domain.aggregates

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.aggregates.payment.events.AuthorizationFailedEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.AuthorizedEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.CaptureFailedEvent
import com.mypay.paymentgateway.domain.aggregates.payment.events.CapturedEvent
import com.mypay.paymentgateway.domain.errors.CaptureNotAllowed
import com.mypay.paymentgateway.domain.errors.InsufficientFunds
import com.mypay.paymentgateway.domain.errors.PaymentAlreadyAuthorized
import com.mypay.paymentgateway.domain.ports.driven.PaymentProcessor
import com.mypay.paymentgateway.domain.ports.driver.AuthorizeCommand
import com.mypay.paymentgateway.domain.ports.driver.CaptureCommand
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
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class PaymentTest {

    private val authorizationAmount = Money(Currency.getInstance("EUR"), 100.0)
    private val captureAmount = 50.0
    private val cardHolder = CardHolder(
        AnagraphicDetails("John", "Doe"),
        BillingDetails(Country("IT"), City("Milan"), Address("Via di Casa Mia")),
        Email("itsme@mail.com")
    )
    private val creditCard =
        CreditCard("4111111111111111", "123", CreditCard.CardExpiration(1, 2030), CreditCard.CardType.VISA)
    private val order = Order("orderID", "Wonderful Hotel, reservation for 2 people, 3 nights from 01/01/2025")
    private val paymentProcessor = mockk<PaymentProcessor>()

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should authorize 100 EUR`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        every { paymentProcessor.authorize(authorizationAmount, cardHolder, creditCard) } returns Ok(AuthID("authID"))

        val payment = Payment(aggregateID)
        val authorizationResult = payment.authorize(
            AuthorizeCommand(
                aggregateID,
                authorizationAmount,
                cardHolder,
                creditCard,
                order,
                paymentProcessor
            )
        )

        assertThat(authorizationResult.isOk).isTrue()
        assertThat(payment.isAuthorized()).isTrue()
        assertThat(payment.getAuthorizedAmount()).isEqualTo(authorizationAmount)
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(AuthorizedEvent::class.java).hasSize(1)
    }

    @Test
    fun `should raise an event when authorization fails`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val pspResponse = InsufficientFunds
        every { paymentProcessor.authorize(authorizationAmount, cardHolder, creditCard) } returns Err(pspResponse)

        val payment = Payment(aggregateID)
        val authorizationResult = payment.authorize(
            AuthorizeCommand(
                aggregateID,
                authorizationAmount,
                cardHolder,
                creditCard,
                order,
                paymentProcessor
            )
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.getError()).isEqualTo(pspResponse)
        assertThat(payment.isAuthorized()).isFalse()
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(AuthorizationFailedEvent::class.java).hasSize(1)
    }

    @Test
    fun `should not authorize a payment already authorized`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentAuthorized(aggregateID)

        val authorizationResult = payment.authorize(
            AuthorizeCommand(
                aggregateID,
                authorizationAmount,
                cardHolder,
                creditCard,
                order,
                paymentProcessor
            )
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.getError()).isEqualTo(PaymentAlreadyAuthorized)
        assertThat(payment.getUncommitedChanges()).hasSize(0)
    }

    @Test
    fun `should capture 50 EUR on a payment authorized for 100 EUR`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        every { paymentProcessor.capture(AuthID("authID"), any()) } returns Ok(CaptureID("captureID"))

        val payment = aPaymentAuthorized(aggregateID)
        val captureResult = payment.capture(
            CaptureCommand(
                aggregateID,
                captureAmount,
                paymentProcessor
            )
        )

        assertThat(captureResult.isOk).isTrue()
        assertThat(payment.isCaptured()).isTrue()
        assertThat(payment.getCapturedAmount()).isEqualTo(Money(payment.getAuthorizedAmount().currency, captureAmount))
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(CapturedEvent::class.java).hasSize(1)
        verify { paymentProcessor.capture(AuthID("authID"), captureAmount) }
    }

    @Test
    fun `should do multiple captures on a payment authorized for 100 EUR`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        every { paymentProcessor.capture(AuthID("authID"), any()) } returns Ok(CaptureID(UUID.randomUUID().toString()))

        val payment = aPaymentAuthorized(aggregateID)
        var captureResult = payment.capture(
            CaptureCommand(
                aggregateID,
                captureAmount,
                paymentProcessor
            )
        )
        captureResult = payment.capture(
            CaptureCommand(
                aggregateID,
                captureAmount,
                paymentProcessor
            )
        )

        assertThat(captureResult.isOk).isTrue()
        assertThat(payment.isCaptured()).isTrue()
        assertThat(payment.getCapturedAmount()).isEqualTo(Money(payment.getAuthorizedAmount().currency, captureAmount*2))
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(CapturedEvent::class.java).hasSize(2)
        verify { paymentProcessor.capture(AuthID("authID"), captureAmount) }
    }

    @Test
    fun `should not capture when a payment is not previously authorized`() {
        val aggregateID = AggregateID(UUID.randomUUID())

        val payment = Payment(aggregateID)
        val captureResult = payment.capture(
            CaptureCommand(
                aggregateID,
                captureAmount,
                paymentProcessor
            )
        )

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.error).isEqualTo(CaptureNotAllowed)
        assertThat(payment.isCaptured()).isFalse()
        assertThat(payment.getUncommitedChanges()).hasSize(0)
    }

    @Test
    fun `should raise an event when capture fail`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        every { paymentProcessor.capture(AuthID("authID"), any()) } returns Err(InsufficientFunds)
        val payment = aPaymentAuthorized(aggregateID)

        val captureResult = payment.capture(
            CaptureCommand(
                aggregateID,
                captureAmount,
                paymentProcessor
            )
        )

        assertThat(captureResult.isErr).isTrue()
        assertThat(payment.isCaptured()).isFalse()
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(CaptureFailedEvent::class.java).hasSize(1)
        verify { paymentProcessor.capture(AuthID("authID"), captureAmount) }
    }

    @Test
    fun `should increase version when changes are marked as committed`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        every { paymentProcessor.capture(AuthID("authID"), any()) } returns Ok(CaptureID("captureID"))

        val payment = aPaymentAuthorized(aggregateID)
        payment.capture(
            CaptureCommand(
                aggregateID,
                captureAmount,
                paymentProcessor
            )
        )

        assertThat(payment.getUncommitedChanges()).hasSize(1)
        val expectedVersion = payment.version
        payment.markChangesAsCommitted()
        assertThat(payment.version).isEqualTo(expectedVersion + 1)
    }


    private fun aPaymentAuthorized(aggregateID: AggregateID): Payment {
        val payment = Payment(aggregateID)
        payment.replayEvents(
            listOf(
                AuthorizedEvent(
                    aggregateID,
                    1,
                    authorizationAmount,
                    cardHolder,
                    creditCard,
                    order,
                    AuthID("authID")
                )
            )
        )
        return payment
    }
}