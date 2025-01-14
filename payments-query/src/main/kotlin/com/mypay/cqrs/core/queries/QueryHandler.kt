package com.mypay.cqrs.core.queries

import com.mypay.cqrs.core.entities.Entity

interface QueryHandler<T : Query, U : Entity> {
    fun handle(query: T): List<U>
}