package com.mypay.cqrs.core.entities

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class Entity(
    @Id
    val id: String
)