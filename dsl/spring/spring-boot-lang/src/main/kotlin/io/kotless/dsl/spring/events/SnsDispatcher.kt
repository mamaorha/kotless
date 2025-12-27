package io.kotless.dsl.spring.events

import io.kotless.InternalAPI
import io.kotless.dsl.cloud.aws.SNSEventData
import org.slf4j.LoggerFactory

@InternalAPI
object SnsDispatcher {
    private val logger = LoggerFactory.getLogger(SnsDispatcher::class.java)

    fun process(snsEvent: SNSEventData) {
        logger.info("received SNSEventData")

        snsEvent.records.forEach { record ->
            val topic = record.sns.topicArn.split(":").last()
            val handlers = SnsConsumersByTopic.snsConsumers[topic]

            if (handlers.isNullOrEmpty()) {
                logger.warn("couldn't find sns handlers for topic: {}", topic)
            } else {
                handlers.forEach { handler ->
                    Result.runCatching {
                        handler(record)
                    }.onFailure {
                        logger.error("sns handler failure", it)
                    }
                }
            }
        }
    }
}
