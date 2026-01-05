package io.kotless.gen.factory.aws.event

import io.kotless.*
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.aws.info.InfoFactory
import io.kotless.gen.factory.aws.resource.dynamic.LambdaFactory
import io.kotless.terraform.functions.link
import io.terraformkt.aws.data.sns.sns_topic
import io.terraformkt.aws.provider.provider
import io.terraformkt.aws.resource.lambda.lambda_permission
import io.terraformkt.aws.resource.sns.sns_topic_subscription
import io.terraformkt.hcl.ref

@OptIn(InternalAPI::class)
object SNSEventsFactory : GenerationFactory<Application.Events.SNS, Unit> {
    override fun mayRun(entity: Application.Events.SNS, context: GenerationContext): Boolean {
        return context.output.check(context.schema.lambdas[entity.lambda]!!, LambdaFactory) &&
               context.output.check(context.schema.application, InfoFactory)
    }

    override fun generate(entity: Application.Events.SNS, context: GenerationContext): GenerationFactory.GenerationResult<Unit> {
        val lambda = context.output.get(context.schema.lambdas[entity.lambda]!!, LambdaFactory)
        val info = context.output.get(context.schema.application, InfoFactory)
        
        val defaultRegion = (context.schema.config.cloud as KotlessConfig.Cloud.AWS).terraform.provider.region
        val topicRegion = entity.region
        
        // Create a provider alias if the topic is in a different region
        val regionProvider = if (topicRegion != defaultRegion) {
            provider {
                alias = topicRegion.replace("-", "_")
                profile = (context.schema.config.cloud as KotlessConfig.Cloud.AWS).terraform.provider.profile
                region = topicRegion
                version = (context.schema.config.cloud as KotlessConfig.Cloud.AWS).terraform.provider.version
            }
        } else {
            null
        }
        
        // Get or reference the SNS topic
        val topic = sns_topic(context.names.tf("sns", entity.topicName)) {
            regionProvider?.let {
                provider = link(it.hcl_ref)
            }
            name = entity.topicName
        }
        
        // Create Lambda permission to allow SNS to invoke the function
        val permission = lambda_permission(context.names.tf(entity.fqId)) {
            statement_id = context.names.aws(entity.fqId)
            action = "lambda:InvokeFunction"
            function_name = lambda.lambda_arn
            principal = "sns.amazonaws.com"
            source_arn = topic::arn.ref
        }
        
        // Create SNS topic subscription
        val subscription = sns_topic_subscription(context.names.tf(entity.fqId)) {
            regionProvider?.let {
                provider = link(it.hcl_ref)
            }
            topic_arn = topic::arn.ref
            protocol = "lambda"
            endpoint = lambda.lambda_arn
        }
        
        val result = if (regionProvider != null) {
            GenerationFactory.GenerationResult(Unit, regionProvider, topic, permission, subscription)
        } else {
            GenerationFactory.GenerationResult(Unit, topic, permission, subscription)
        }
        
        return result
    }
}

