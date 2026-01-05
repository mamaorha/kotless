package io.kotless.parser.spring.processor.event

import io.kotless.Application.Events
import io.kotless.InternalAPI
import io.kotless.dsl.cloud.aws.SNSEvent
import io.kotless.parser.processor.AnnotationProcessor
import io.kotless.parser.processor.ProcessorContext
import io.kotless.parser.processor.config.EntrypointProcessor
import io.kotless.parser.processor.permission.PermissionsProcessor
import io.kotless.parser.utils.psi.annotation.getValue
import io.kotless.resource.Lambda
import io.kotless.utils.TypedStorage
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

@OptIn(InternalAPI::class)
internal object SNSEventsProcessor : AnnotationProcessor<Unit>() {
    override val annotations = setOf(SNSEvent::class)

    override fun mayRun(context: ProcessorContext) = context.output.check(EntrypointProcessor)

    override fun process(files: Set<KtFile>, binding: BindingContext, context: ProcessorContext) {
        processStaticFunctions(files, binding) { function, annotation, _ ->
            val entrypoint = context.output.get(EntrypointProcessor).entrypoint
            
            val topicName = annotation.getValue(binding, SNSEvent::topicName)!!
            val region = annotation.getValue(binding, SNSEvent::region) ?: ""
            val effectiveRegion = region.ifEmpty {
                context.config.cloud.let {
                    (it as io.kotless.KotlessConfig.Cloud.AWS).terraform.provider.region
                }
            }
            
            val permissions = PermissionsProcessor.process(files, function, binding, context)
            val name = function.fqName!!.asString()
            
            val key = TypedStorage.Key<Lambda>()
            val lambda = Lambda(name, context.jar, entrypoint, context.lambda, permissions)
            
            context.resources.register(key, lambda)
            context.events.register(
                Events.SNS(name, topicName, effectiveRegion, key)
            )
        }
    }
}

