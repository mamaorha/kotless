package io.kotless.dsl.cloud.aws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** AWS SNS event representation */
@Serializable
data class SNSEventData(
    @SerialName("Records") val records: List<SNSRecord>
) {
    @Serializable
    data class SNSRecord(
        @SerialName("EventSource") val eventSource: String,
        @SerialName("EventVersion") val eventVersion: String,
        @SerialName("EventSubscriptionArn") val eventSubscriptionArn: String,
        @SerialName("Sns") val sns: SNSMessage
    ) {
        @Serializable
        data class SNSMessage(
            @SerialName("Type") val type: String,
            @SerialName("MessageId") val messageId: String,
            @SerialName("TopicArn") val topicArn: String,
            @SerialName("Subject") val subject: String? = null,
            @SerialName("Message") val message: String,
            @SerialName("Timestamp") val timestamp: String,
            @SerialName("SignatureVersion") val signatureVersion: String,
            @SerialName("Signature") val signature: String,
            @SerialName("SigningCertUrl") val signingCertUrl: String,
            @SerialName("UnsubscribeUrl") val unsubscribeUrl: String,
            @SerialName("MessageAttributes") val messageAttributes: Map<String, MessageAttribute>? = null
        ) {
            @Serializable
            data class MessageAttribute(
                @SerialName("Type") val type: String,
                @SerialName("Value") val value: String
            )
        }
    }
}

