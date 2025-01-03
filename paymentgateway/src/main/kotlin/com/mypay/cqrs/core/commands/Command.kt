package com.mypay.cqrs.core.commands

import com.mypay.cqrs.core.aggregates.AggregateID

abstract class Command(val aggregateId: AggregateID)