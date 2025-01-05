package com.mypay.paymentgateway.application.handlers

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.handlers.EventSourcingHandler
import com.mypay.paymentgateway.application.commands.Capture
import com.mypay.paymentgateway.domain.errors.InsufficientFunds
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyViolation
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.services.FixedPercentageMerchantFees
import com.mypay.paymentgateway.domain.services.MerchantFees
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class CaptureHandlerTest {
    @Test
    fun `should capture and save aggregate`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val merchantFees = mockk<MerchantFees>()
        val aggregate = mockk<Payment>()

        every { eventSourcingHandler.getById(any()) } returns aggregate
        every { aggregate.capture(any(), any()) } returns Ok(Unit)
        every { eventSourcingHandler.save(any()) } returns Ok(Unit)

        val captureResult = CaptureHandler(eventSourcingHandler, merchantFees).handle(
            Capture(
                AggregateID(UUID.randomUUID()),
                100.00
            )
        )

        assertThat(captureResult.isOk).isTrue()

        verify { aggregate.capture(any(), any()) }
        verify { eventSourcingHandler.save(any()) }
    }

    @Test
    fun `should save aggregate even when capture fails`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val merchantFees = mockk<MerchantFees>()
        val aggregate = mockk<Payment>()
        val captureError = InsufficientFunds

        every { eventSourcingHandler.getById(any()) } returns aggregate
        every { aggregate.capture(any(), any()) } returns Err(captureError)
        every { eventSourcingHandler.save(any()) } returns Ok(Unit)

        val captureResult = CaptureHandler(eventSourcingHandler, merchantFees).handle(
            Capture(
                AggregateID(UUID.randomUUID()),
                100.00
            )
        )

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.getError()).isEqualTo(captureError)

        verify { aggregate.capture(any(), any()) }
        verify { eventSourcingHandler.save(any()) }
    }

    @Test
    fun `should return an error when aggregate saving fails`() {
        val eventSourcingHandler = mockk<EventSourcingHandler<Payment>>()
        val merchantFees = mockk<MerchantFees>()
        val eventSourcingHandlerResult = OptimisticConcurrencyViolation
        val aggregate = mockk<Payment>()

        every { eventSourcingHandler.getById(any()) } returns aggregate
        every { aggregate.capture(any(), any()) } returns Ok(Unit)
        every { eventSourcingHandler.save(any()) } returns Err(eventSourcingHandlerResult)

        val captureResult = CaptureHandler(eventSourcingHandler, merchantFees).handle(
            Capture(
                AggregateID(UUID.randomUUID()),
                100.00
            )
        )

        assertThat(captureResult.isErr).isTrue()
        assertThat(captureResult.getError()).isEqualTo(eventSourcingHandlerResult)

        verify { aggregate.capture(any(), any()) }
        verify { eventSourcingHandler.save(any()) }
    }
}