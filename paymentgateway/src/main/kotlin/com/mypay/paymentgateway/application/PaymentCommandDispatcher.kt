package com.mypay.paymentgateway.application

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.mypay.cqrs.core.commands.Command
import com.mypay.cqrs.core.commands.CommandHandler
import com.mypay.cqrs.core.infrastructure.CommandDispatcher
import com.mypay.paymentgateway.domain.errors.CommandNotFound
import com.mypay.paymentgateway.domain.errors.DomainError
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentCommandDispatcher : CommandDispatcher {
    private val logger = LoggerFactory.getLogger(PaymentCommandDispatcher::class.java)
    private val routes = HashMap<Class<out Command>, CommandHandler<out Command>>()

    override fun <T : Command> registerHandler(type: Class<T>, handler: CommandHandler<T>) {
        routes[type] = handler
    }

    override fun send(command: Command): Result<Unit, DomainError> {
        val handler = routes[command::class.java]
        if (handler == null) {
            logger.error("No handler found for command " + command.javaClass.name)
            return Err(CommandNotFound)
        }
        return (handler as CommandHandler<Command>).handle(command)
    }
}