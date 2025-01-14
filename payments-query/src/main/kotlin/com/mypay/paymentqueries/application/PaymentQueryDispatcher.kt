package com.mypay.paymentqueries.application

import com.mypay.cqrs.core.entities.Entity
import com.mypay.cqrs.core.infrastructure.QueryDispatcher
import com.mypay.cqrs.core.queries.Query
import com.mypay.cqrs.core.queries.QueryHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PaymentQueryDispatcher : QueryDispatcher {
    private val logger = LoggerFactory.getLogger(PaymentQueryDispatcher::class.java)
    private val routes = HashMap<Class<out Query>, QueryHandler<out Query, out Entity>>()

    override fun <T : Query, U : Entity> registerHandler(type: Class<T>, handler: QueryHandler<T, U>) {
        routes[type] = handler
    }

    override fun <U : Entity> send(query: Query): List<U> {
        val handler = routes[query::class.java]
        if (handler == null) {
            logger.error("No handler found for query " + query.javaClass.name)
            throw IllegalStateException("No handler found for query " + query.javaClass.name)
        }
        return (handler as QueryHandler<Query, U>).handle(query)
    }
}