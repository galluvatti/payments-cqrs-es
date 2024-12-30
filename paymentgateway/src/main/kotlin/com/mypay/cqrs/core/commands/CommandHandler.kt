package com.mypay.cqrs.core.commands

interface CommandHandler<T> where T : Command {
    fun handle(command: T)
}