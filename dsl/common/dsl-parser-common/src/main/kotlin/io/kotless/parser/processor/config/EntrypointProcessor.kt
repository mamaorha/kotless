package io.kotless.parser.processor.config

import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import io.kotless.InternalAPI
import io.kotless.parser.processor.ProcessorContext
import io.kotless.parser.processor.SubTypesProcessor
import io.kotless.parser.utils.errors.require
import io.kotless.parser.utils.psi.isSubtypeOf
import io.kotless.resource.Lambda
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext

@OptIn(InternalAPI::class)
object EntrypointProcessor : SubTypesProcessor<EntrypointProcessor.Output>() {
    data class Output(val entrypoint: Lambda.Entrypoint)

    override val klasses = setOf(RequestStreamHandler::class)

    override fun mayRun(context: ProcessorContext) = true

    override fun process(files: Set<KtFile>, binding: BindingContext, context: ProcessorContext): Output {
        return Output(find(files, binding))
    }

    fun find(files: Set<KtFile>, binding: BindingContext): Lambda.Entrypoint {
        val entrypoint = ArrayList<Lambda.Entrypoint>()
        processClassesOrObjects(files, binding) { klass, _ ->
            entrypoint.add(klass.makeLambdaEntrypoint(binding))
        }

        require(entrypoint.isNotEmpty()) { "There should be a class or object inherited from ${RequestStreamHandler::class} in your app" }
        require(entrypoint.size == 1) { "There should be only one class or object inherited from ${RequestStreamHandler::class} in your app" }

        return entrypoint.first()
    }

    private fun KtClassOrObject.makeLambdaEntrypoint(binding: BindingContext): Lambda.Entrypoint {
        require(this, fqName != null) { "Anonymous class cannot be inherited from RequestStreamHandler" }
        if (this.isSubtypeOf(RequestStreamHandler::class, binding)) {
            return Lambda.Entrypoint("${fqName!!.asString()}::${RequestStreamHandler::handleRequest.name}")
        }
        error("Entry point should be inherited from ${RequestStreamHandler::class}")
    }
}
