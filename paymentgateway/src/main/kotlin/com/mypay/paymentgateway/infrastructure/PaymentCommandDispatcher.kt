package com.mypay.paymentgateway.infrastructure

import com.github.michaelbull.result.Result
import com.mypay.cqrs.core.commands.Command
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.infrastructure.CommandDispatcher
import com.mypay.paymentgateway.domain.errors.PaymentGatewayError
import org.springframework.stereotype.Service

@Service
class PaymentCommandDispatcher : CommandDispatcher {
    override fun <T : Command> registerHandler(type: Class<T>, handler: CommandHandler<T>) {
        routes[type] = handler
    }

    private val routes = HashMap<Class<out Command>, CommandHandler<out Command>>()

    override fun send(command: Command): Result<Unit, PaymentGatewayError> {
        val handler = routes[command::class.java]
            ?: throw RuntimeException("No handler found for the command ${command::class.java}")
        return (handler as CommandHandler<Command>).handle(command);
    }
}