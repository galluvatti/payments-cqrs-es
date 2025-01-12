package com.mypay.cqrs.core.infrastructure

import com.mypay.cqrs.core.entities.Entity
import com.mypay.cqrs.core.queries.Query
import com.mypay.cqrs.core.queries.QueryHandler

interface QueryDispatcher {
    fun <T> registerHandler(type: Class<T>, handler: QueryHandler<T>) where T : Query
    fun <U> send(query: Query): List<U> where U : Entity
}