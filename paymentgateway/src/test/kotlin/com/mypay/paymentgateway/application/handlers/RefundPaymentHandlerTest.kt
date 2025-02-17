package com.mypay.paymentgateway.application.handlers

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.handlers.EventSourcingRepository
import com.mypay.paymentgateway.application.commands.RefundPayment
import com.mypay.paymentgateway.domain.payment.Payment
import com.mypay.paymentgateway.domain.errors.InsufficientFunds
import com.mypay.paymentgateway.domain.errors.OptimisticConcurrencyViolation
import com.mypay.paymentgateway.domain.services.RefundPolicy
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class RefundPaymentHandlerTest {
    @Test
    fun `should refund and save aggregate`() {
        val eventSourcingRepository = mockk<EventSourcingRepository<Payment>>()
        val refundPolicy = mockk<RefundPolicy>()
        val aggregate = mockk<Payment>()

        every { eventSourcingRepository.getById(any()) } returns aggregate
        every { aggregate.refund(any(), refundPolicy) } returns Ok(Unit)
        every { eventSourcingRepository.save(any()) } returns Ok(Unit)

        val refundResult = RefundPaymentHandler(eventSourcingRepository, refundPolicy).handle(
            RefundPayment(
                AggregateID(UUID.randomUUID()),
                100.00
            )
        )

        assertThat(refundResult.isOk).isTrue()

        verify { aggregate.refund(any(), refundPolicy) }
        verify { eventSourcingRepository.save(any()) }
    }

    @Test
    fun `should save aggregate even when refund fails`() {
        val eventSourcingRepository = mockk<EventSourcingRepository<Payment>>()
        val refundPolicy = mockk<RefundPolicy>()
        val aggregate = mockk<Payment>()
        val refundError = InsufficientFunds

        every { eventSourcingRepository.getById(any()) } returns aggregate
        every { aggregate.refund(any(), refundPolicy) } returns Err(refundError)
        every { eventSourcingRepository.save(any()) } returns Ok(Unit)

        val refundResult = RefundPaymentHandler(eventSourcingRepository, refundPolicy).handle(
            RefundPayment(
                AggregateID(UUID.randomUUID()),
                100.00
            )
        )

        assertThat(refundResult.isErr).isTrue()
        assertThat(refundResult.getError()).isEqualTo(refundError)

        verify { aggregate.refund(any(), refundPolicy) }
        verify { eventSourcingRepository.save(any()) }
    }

    @Test
    fun `should return an error when aggregate saving fails`() {
        val eventSourcingRepository = mockk<EventSourcingRepository<Payment>>()
        val refundPolicy = mockk<RefundPolicy>()
        val eventSourcingHandlerResult = OptimisticConcurrencyViolation
        val aggregate = mockk<Payment>()

        every { eventSourcingRepository.getById(any()) } returns aggregate
        every { aggregate.refund(any(), refundPolicy) } returns Ok(Unit)
        every { eventSourcingRepository.save(any()) } returns Err(eventSourcingHandlerResult)

        val refundResult = RefundPaymentHandler(eventSourcingRepository, refundPolicy).handle(
            RefundPayment(
                AggregateID(UUID.randomUUID()),
                100.00
            )
        )

        assertThat(refundResult.isErr).isTrue()
        assertThat(refundResult.getError()).isEqualTo(eventSourcingHandlerResult)

        verify { aggregate.refund(any(), refundPolicy) }
        verify { eventSourcingRepository.save(any()) }
    }
}