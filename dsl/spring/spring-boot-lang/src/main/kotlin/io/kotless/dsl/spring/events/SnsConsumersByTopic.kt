package io.kotless.dsl.spring.events

import io.kotless.dsl.cloud.aws.SNSEventData
import org.slf4j.LoggerFactory

object SnsConsumersByTopic {
    private val log = LoggerFactory.getLogger(SnsConsumersByTopic::class.java)

    val snsConsumers: Map<String, List<(SNSEventData.SNSRecord) -> Unit>> = emptyMap()

    init {
        val message = "Initial SnsConsumersByTopic should never be called"
        log.error(message)

        error(message)
    }
}
