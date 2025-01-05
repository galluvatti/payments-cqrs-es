package com.mypay.paymentgateway.adapters.kafka

import com.google.gson.*
import com.mypay.cqrs.core.events.DomainEvent
import com.mypay.cqrs.core.infrastructure.EventProducer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class PaymentEventProducer(
    @Autowired @Value("\${spring.kafka.topic}") private val topic: String,
    @Autowired private val kafkaTemplate: KafkaTemplate<String, Any>

) : EventProducer {
    private val logger = LoggerFactory.getLogger(PaymentEventProducer::class.java)
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter())
        .create()

    override fun produce(event: DomainEvent) {
        logger.info("Sending event $event to Kafka topic $topic")
        kafkaTemplate.send(topic, gson.toJson(event))
    }
}

class LocalDateTimeAdapter : JsonSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun serialize(
        src: LocalDateTime?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src?.format(formatter))
    }
}