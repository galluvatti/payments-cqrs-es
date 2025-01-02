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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class CaptureCommandHandlerTest {
    @Test
    fun `should capture and save aggregate`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val paymentProcessor = mockk<PaymentProcessor>()
        val aggregate = mockk<Payment>()

        every { eventSourcingHandler.getById(any()) } returns aggregate
        every { aggregate.capture(any()) }returns Ok(Unit)
        every { eventSourcingHandler.save(any()) } returns Ok(Unit)

        val captureResult = CaptureCommandHandler(eventSourcingHandler).handle(
            CaptureCommand(
                AggregateID(UUID.randomUUID()),
                100.00,
                paymentProcessor
            )
        )

        assertThat(captureResult.isOk).isTrue()

        verify { aggregate.capture(any()) }
        verify { eventSourcingHandler.save(any()) }
    }

    @Test
    fun `should save aggregate even when capture fails`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val paymentProcessor = mockk<PaymentProcessor>()
        val aggregate = mockk<Payment>()
        val paymentProcessorResult = InsufficientFunds

        every { eventSourcingHandler.getById(any()) } returns aggregate
        every { aggregate.capture(any()) }returns Err(paymentProcessorResult)
        every { eventSourcingHandler.save(any()) } returns Ok(Unit)

        val captureResult = CaptureCommandHandler(eventSourcingHandler).handle(
            CaptureCommand(
                AggregateID(UUID.randomUUID()),
                100.00,
                paymentProcessor
            )
        )

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.getError()).isEqualTo(paymentProcessorResult)

        verify { aggregate.capture(any()) }
        verify { eventSourcingHandler.save(any()) }
    }

    @Test
    fun `should return an error when aggregate saving fails`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val paymentProcessor = mockk<PaymentProcessor>()
        val eventSourcingHandlerResult = OptimisticConcurrencyViolation
        val aggregate = mockk<Payment>()

        every { eventSourcingHandler.getById(any()) } returns aggregate
        every { aggregate.capture(any()) }returns Ok(Unit)
        every { eventSourcingHandler.save(any()) } returns Err(eventSourcingHandlerResult)

        val captureResult = CaptureCommandHandler(eventSourcingHandler).handle(
            CaptureCommand(
                AggregateID(UUID.randomUUID()),
                100.00,
                paymentProcessor
            )
        )

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.getError()).isEqualTo(eventSourcingHandlerResult)

        verify { aggregate.capture(any()) }
        verify { eventSourcingHandler.save(any()) }
    }
}