package com.mypay.cqrs.core.infrastructure

import com.mypay.cqrs.core.commands.Command
import com.mypay.cqrs.core.commands.CommandHandler

interface CommandDispatcher {
    fun <T> registerHandler(type: Class<T>, handler: CommandHandler<T>) where T : Command
    fun send(command: Command)
}