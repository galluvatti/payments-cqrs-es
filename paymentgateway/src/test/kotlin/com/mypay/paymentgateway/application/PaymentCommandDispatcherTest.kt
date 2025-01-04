package com.mypay.paymentgateway.application

import com.github.michaelbull.result.Ok
import com.mypay.cqrs.core.aggregates.AggregateID
import com.mypay.cqrs.core.commands.Command
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.paymentgateway.domain.errors.CommandNotFound
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class PaymentCommandDispatcherTest {

    @Test
    fun `should register commands and dispatch them correctly`() {
        val fakeCommandHandler = mockk<CommandHandler<AFakeCommand>>()
        val anotherFakeCommandHandler = mockk<CommandHandler<AnotherFakeCommand>>()
        every { fakeCommandHandler.handle(any()) } returns Ok(Unit)
        every { anotherFakeCommandHandler.handle(any()) } returns Ok(Unit)

        val commandDispatcher = PaymentCommandDispatcher()
        commandDispatcher.registerHandler(AFakeCommand::class.java, fakeCommandHandler)
        commandDispatcher.registerHandler(AnotherFakeCommand::class.java, anotherFakeCommandHandler)

        commandDispatcher.send(AFakeCommand(AggregateID(UUID.randomUUID())))
        commandDispatcher.send(AnotherFakeCommand(AggregateID(UUID.randomUUID())))

        verifyOrder {
            fakeCommandHandler.handle(any())
            anotherFakeCommandHandler.handle(any())
        }
    }

    @Test
    fun `should return an error when a command is not mapped to a handler`() {
        val fakeCommandHandler = mockk<CommandHandler<AFakeCommand>>()

        val commandDispatcher = PaymentCommandDispatcher()
        commandDispatcher.registerHandler(AFakeCommand::class.java, fakeCommandHandler)

        val result = commandDispatcher.send(AnotherFakeCommand(AggregateID(UUID.randomUUID())))

        assertThat(result.isErr).isTrue()
        assertThat(result.error).isEqualTo(CommandNotFound)
    }

    class AFakeCommand(aggregateID: AggregateID) : Command(aggregateID)
    class AnotherFakeCommand(aggregateID: AggregateID) : Command(aggregateID)
}