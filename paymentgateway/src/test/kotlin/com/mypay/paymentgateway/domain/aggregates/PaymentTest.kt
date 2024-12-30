package com.mypay.paymentgateway.domain.aggregates

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.paymentgateway.domain.aggregates.payment.Payment
import com.mypay.paymentgateway.domain.aggregates.payment.events.AuthorizedPaymentEvent
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
    private val captureAmount = Money(Currency.getInstance("EUR"), 50.0)
    private val cardHolder = CardHolder(
        AnagraphicDetails("John", "Doe"),
        BillingDetails(Country("IT"), City("Milan"), Address("Via di Casa Mia", "44")),
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

        val payment = Payment(aggregateID, paymentProcessor)
        val authorizationResult = payment.authorize(
            AuthorizeCommand(
                aggregateID,
                authorizationAmount,
                cardHolder,
                creditCard,
                order
            )
        )

        assertThat(authorizationResult.isOk).isTrue()
        assertThat(payment.isAuthorized()).isTrue()
        assertThat(payment.getAuthorizedAmount()).isEqualTo(authorizationAmount)
        assertThat(payment.version).isEqualTo(1)
    }

    @Test
    fun `should return an error when authorization fails`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val pspResponse = InsufficientFunds
        every { paymentProcessor.authorize(authorizationAmount, cardHolder, creditCard) } returns Err(pspResponse)

        val payment = Payment(aggregateID, paymentProcessor)
        val authorizationResult = payment.authorize(
            AuthorizeCommand(
                aggregateID,
                authorizationAmount,
                cardHolder,
                creditCard,
                order
            )
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.getError()).isEqualTo(pspResponse)
        assertThat(payment.isAuthorized()).isFalse()
        assertThat(payment.version).isEqualTo(0)
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
                order
            )
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.getError()).isEqualTo(PaymentAlreadyAuthorized)
    }

    @Test
    fun `should capture 50 EUR on a payment authorized for 100 EUR`() {
        val aggregateID = AggregateID(UUID.randomUUID())

        val payment = aPaymentAuthorized(aggregateID)
        every { paymentProcessor.capture(any()) } returns Ok(CaptureID("captureID"))

        val captureResult = payment.capture(
            CaptureCommand(
                aggregateID,
                captureAmount,
            )
        )

        assertThat(captureResult.isOk).isTrue()
        assertThat(payment.isCaptured()).isTrue()
        assertThat(payment.getCapturedAmount()).isEqualTo(captureAmount)
        verify { paymentProcessor.capture(captureAmount) }
    }


    private fun aPaymentAuthorized(aggregateID: AggregateID): Payment {
        val payment = Payment(aggregateID, paymentProcessor)
        payment.replayEvents(
            listOf(
                AuthorizedPaymentEvent(
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