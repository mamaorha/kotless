package io.kotless.gen.factory.azure.event

import io.kotless.Application
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.azure.filescontent.LambdaDescription.timeBinding
import io.kotless.gen.factory.azure.utils.FilesCreationTf
import io.kotless.terraform.functions.path
import java.util.*


object ScheduledEventsFactory : GenerationFactory<Application.Events.Scheduled, ScheduledEventsFactory.Output> {

    data class Output(val fileCreationRef: String)

    override fun mayRun(entity: Application.Events.Scheduled, context: GenerationContext): Boolean = true

    override fun generate(entity: Application.Events.Scheduled, context: GenerationContext): GenerationFactory.GenerationResult<Output> {
        val lambda = context.schema.lambdas[entity.lambda]!!
        val binding = timeBinding(lambda, entity.cron)
        val timerName = entity.fqId
        val resourceName = "timer_binding_$timerName"
        val path = timerName
        val result = FilesCreationTf.localFile(resourceName, binding, path(lambda.file.parentFile.resolve(path).resolve("function.json")))

        return GenerationFactory.GenerationResult(Output(result.hcl_ref), result)
    }
}
