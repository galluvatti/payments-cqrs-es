package com.mypay.paymentqueries.adapters.kafka

import com.google.gson.*
import com.mypay.paymentqueries.adapters.kafka.events.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class PaymentEventConsumer(
    @Autowired private val eventHandler: PaymentEventHandler
) {
    private val logger = LoggerFactory.getLogger(PaymentEventConsumer::class.java)

    private val gson = GsonBuilder().registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeAdapter()).create()

    @KafkaListener(
        topics = ["\${spring.kafka.topic}"],
        groupId = "\${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )

    fun consumePaymentEvent(@Payload event: String, ack: Acknowledgment) {
        logger.info("Consuming new event $event")
        val domainEvent = gson.fromJson(event, Map::class.java)
        val eventType = domainEvent["eventType"]

        when (eventType) {
            PaymentAuthorized::class.java.simpleName -> eventHandler.consume(
                gson.fromJson(
                    event,
                    PaymentAuthorized::class.java
                ), ack
            )

            FraudDetected::class.java.simpleName -> eventHandler.consume(
                gson.fromJson(
                    event,
                    FraudDetected::class.java
                ), ack
            )

            PaymentCaptured::class.java.simpleName -> eventHandler.consume(
                gson.fromJson(
                    event,
                    PaymentCaptured::class.java
                ), ack
            )

            PaymentCaptureFailed::class.java.simpleName -> eventHandler.consume(
                gson.fromJson(
                    event,
                    PaymentCaptureFailed::class.java
                ), ack
            )

            PaymentRefunded::class.java.simpleName -> eventHandler.consume(
                gson.fromJson(
                    event,
                    PaymentRefunded::class.java
                ), ack
            )

            PaymentRefundFailed::class.java.simpleName -> eventHandler.consume(
                gson.fromJson(
                    event,
                    PaymentRefundFailed::class.java
                ), ack
            )
        }
    }

}

private class LocalDateTimeAdapter : JsonDeserializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): LocalDateTime {
        return LocalDateTime.parse(json?.asString, formatter)
    }
}