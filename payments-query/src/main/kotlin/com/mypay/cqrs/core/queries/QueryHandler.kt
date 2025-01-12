package com.mypay.cqrs.core.queries

import com.mypay.cqrs.core.entities.Entity

interface QueryHandler<T> where T : Query {
    fun handle(query: T): List<Entity>
}