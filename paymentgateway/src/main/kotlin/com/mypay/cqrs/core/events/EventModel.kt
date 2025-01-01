package com.mypay.cqrs.core.events

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class EventModel(
    @Id
    val id: String? = null,
    val timestamp: Date,
    val aggregateId: String,
    var aggregateType: String,
    val version: Int,
    val eventType: String,
    val eventData: DomainEvent
)