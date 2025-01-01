package com.mypay.cqrs.core.aggregates

import com.mypay.cqrs.core.events.DomainEvent
import org.slf4j.LoggerFactory

abstract class AggregateRoot(
    val id: AggregateID,
    var version: Int = 0
) {

    private val logger = LoggerFactory.getLogger(AggregateRoot::class.java)
    private val uncommittedChanges = ArrayList<DomainEvent>()

    protected fun raiseEvent(event: DomainEvent) {
        applyChange(event, true);
    }

    fun replayEvents(events: Iterable<DomainEvent>) {
        events.forEach { e -> applyChange(e, false) }
    }

    private fun applyChange(event: DomainEvent, isNewEvent: Boolean) {
        try {
            val applyMethod = javaClass.getDeclaredMethod("apply", event.javaClass)
            applyMethod.trySetAccessible()
            applyMethod.invoke(this, event)
            version++
        } catch (e: Exception) {
            logger.error("Error applying event to aggregate ", e)
        } finally {
            if (isNewEvent)
                uncommittedChanges.add(event)
        }
    }

    fun getUncommitedChanges(): List<DomainEvent> {
        return this.uncommittedChanges
    }
    fun markChangesAsCommitted() {
        uncommittedChanges.clear()
    }

}