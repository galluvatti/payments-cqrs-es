package com.mypay.paymentgateway.domain.payment

import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.paymentgateway.domain.errors.*
import com.mypay.paymentgateway.domain.events.*
import com.mypay.paymentgateway.domain.services.FraudInvestigator
import com.mypay.paymentgateway.domain.valueobjects.*
import com.mypay.paymentgateway.domain.valueobjects.address.Address
import com.mypay.paymentgateway.domain.valueobjects.address.City
import com.mypay.paymentgateway.domain.valueobjects.address.Country
import com.mypay.paymentgateway.domain.valueobjects.billing.BillingDetails
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CardHolder
import com.mypay.paymentgateway.domain.valueobjects.creditcard.CreditCard
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class PaymentTest {

    private val authorizationAmount = Money(Currency.getInstance("EUR"), 100.0)
    private val captureAmount = 50.0
    private val refundAmount = 40.0
    private val cardHolder = CardHolder(
        AnagraphicDetails("John", "Doe"),
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
    private val merchant = Merchant("merchantID")

    private val fraudInvestigator = mockk<FraudInvestigator>()

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should authorize 100 EUR`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        every { fraudInvestigator.isFraud(cardHolder, creditCard) } returns false

        val payment = Payment(aggregateID)
        val authorizationResult = payment.authorize(
            merchant,
            authorizationAmount,
            cardHolder,
            creditCard,
            order,
            fraudInvestigator
        )

        assertThat(authorizationResult.isOk).isTrue()
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(Authorized::class.java).hasSize(1)
    }

    @Test
    fun `should detect a fraud`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        every { fraudInvestigator.isFraud(cardHolder, creditCard) } returns true

        val payment = Payment(aggregateID)
        val authorizationResult = payment.authorize(
            merchant,
            authorizationAmount,
            cardHolder,
            creditCard,
            order,
            fraudInvestigator
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.error).isEqualTo(SuspectFraud)
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(FraudDetected::class.java).hasSize(1)
    }

    @Test
    fun `should not authorize a payment already authorized`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentAuthorized(aggregateID)

        val authorizationResult = payment.authorize(
            merchant,
            authorizationAmount,
            cardHolder,
            creditCard,
            order,
            fraudInvestigator
        )

        assertThat(authorizationResult.isErr).isTrue()
        assertThat(authorizationResult.error).isEqualTo(PaymentAlreadyAuthorized)
        assertThat(payment.getUncommitedChanges()).hasSize(0)
    }

    @Test
    fun `should capture 50 EUR on a payment authorized for 100 EUR`() {
        val aggregateID = AggregateID(UUID.randomUUID())

        val payment = aPaymentAuthorized(aggregateID)
        val captureResult = payment.capture(captureAmount)

        assertThat(captureResult.isOk).isTrue()
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(Captured::class.java).hasSize(1)
    }

    @Test
    fun `should not allow a capture when amount exceeds the authorization amount`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentAuthorized(aggregateID)

        val captureResult = payment.capture(authorizationAmount.amount + 0.01)

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.error).isEqualTo(InsufficientFunds)
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(CaptureFailed::class.java).hasSize(1)
    }

    @Test
    fun `should not capture a payment not authorized`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = Payment(aggregateID)

        val captureResult = payment.capture(captureAmount)

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.error).isEqualTo(CaptureNotAllowed)
        assertThat(payment.getUncommitedChanges()).hasSize(0)
    }

    @Test
    fun `should not capture twice`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentCaptured(aggregateID)

        val captureResult = payment.capture(captureAmount)

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.error).isEqualTo(CaptureNotAllowed)
        assertThat(payment.getUncommitedChanges()).hasSize(0)
    }

    @Test
    fun `should not capture a refunded payment`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentRefunded(aggregateID)

        val captureResult = payment.capture(captureAmount)

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.error).isEqualTo(CaptureNotAllowed)
        assertThat(payment.getUncommitedChanges()).hasSize(0)
    }


    @Test
    fun `should refund 40 EUR on a payment captured for 50 EUR`() {
        val aggregateID = AggregateID(UUID.randomUUID())

        val payment = aPaymentCaptured(aggregateID)
        val refundResult = payment.refund(refundAmount)

        assertThat(refundResult.isOk).isTrue()
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(Refunded::class.java).hasSize(1)
    }

    @Test
    fun `should not refund an authorized payment`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentAuthorized(aggregateID)

        val refundResult = payment.refund(refundAmount)

        assertThat(refundResult.isErr).isTrue()
        assertThat(refundResult.error).isEqualTo(RefundNotAllowed)
        assertThat(payment.getUncommitedChanges()).hasSize(0)
    }

    @Test
    fun `should not refund twice`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentRefunded(aggregateID)

        val result = payment.refund(refundAmount)

        assertThat(result.isErr).isTrue()
        assertThat(result.error).isEqualTo(RefundNotAllowed)
        assertThat(payment.getUncommitedChanges()).hasSize(0)
    }

    @Test
    fun `should not allow to refund an amount greater that the captured amount`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentCaptured(aggregateID)

        val refundResult = payment.refund(captureAmount + 0.01)

        assertThat(refundResult.isErr).isTrue()
        assertThat(refundResult.error).isEqualTo(RefundNotAllowed)
        assertThat(payment.getUncommitedChanges())
            .hasOnlyElementsOfType(RefundFailed::class.java).hasSize(1)
    }

    @Test
    fun `should increase version when changes are marked as committed`() {
        val aggregateID = AggregateID(UUID.randomUUID())
        val payment = aPaymentAuthorized(aggregateID)

        payment.capture(captureAmount)

        assertThat(payment.getUncommitedChanges()).hasSize(1)
        val expectedVersion = payment.version
        payment.markChangesAsCommitted()
        assertThat(payment.version).isEqualTo(expectedVersion + 1)
    }

    private fun aPaymentAuthorized(aggregateID: AggregateID): Payment {
        val payment = Payment(aggregateID)
        payment.replayEvents(
            listOf(
                Authorized(
                    aggregateID,
                    0,
                    Merchant("merchantID"),
                    authorizationAmount,
                    cardHolder,
                    creditCard,
                    order
                )
            )
        )
        return payment
    }


    private fun aPaymentCaptured(aggregateID: AggregateID): Payment {
        val payment = Payment(aggregateID)
        payment.replayEvents(
            listOf(
                Authorized(
                    aggregateID,
                    0,
                    Merchant("merchantID"),
                    authorizationAmount,
                    cardHolder,
                    creditCard,
                    order
                ),
                Captured(
                    aggregateID,
                    1,
                    Money(authorizationAmount.currency, captureAmount)
                )
            )
        )
        return payment
    }

    private fun aPaymentRefunded(aggregateID: AggregateID): Payment {
        val payment = Payment(aggregateID)
        payment.replayEvents(
            listOf(
                Authorized(
                    aggregateID,
                    0,
                    Merchant("merchantID"),
                    authorizationAmount,
                    cardHolder,
                    creditCard,
                    order
                ),
                Captured(
                    aggregateID,
                    1,
                    Money(authorizationAmount.currency, captureAmount)
                ),
                Refunded(
                    aggregateID,
                    2,
                    Money(authorizationAmount.currency, refundAmount)
                )
            )
        )
        return payment
    }
}