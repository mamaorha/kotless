package io.kotless.plugin.gradle.spring.resources

import io.kotless.dsl.cloud.aws.SNSEvent
import io.kotless.parser.utils.psi.analysis.*
import io.kotless.parser.utils.psi.annotation.*
import io.kotless.parser.utils.psi.isStatic
import io.kotless.parser.utils.psi.visitNamedFunctions
import io.kotless.plugin.gradle.graal.tasks.GenerateAdapter
import io.kotless.plugin.gradle.utils.gradle.Dependencies
import io.kotless.plugin.gradle.utils.gradle.myKtSourceSet
import org.gradle.api.Project
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

object SnsConsumersByTopicSource {
    val type = GenerateAdapter.SourceType.Kotlin
    val filePath = "io/kotless/dsl/spring/events/SnsConsumersByTopic.kt"

    fun data(project: Project): String {
        val libs = Dependencies.getDependencies(project)
        val sources = project.myKtSourceSet.toSet()

        val environment = EnvironmentManager.create(libs)

        val ktFiles = ParseUtil.analyze(sources, environment)
        val binding = ResolveUtil.analyze(ktFiles, environment).bindingContext
        val annotations = listOf(SNSEvent::class)

        val consumersByTopic = mutableMapOf<String, MutableList<String>>()

        for (file in ktFiles) {
            file.visitNamedFunctions(filter = { function -> function.isAnnotatedWith(binding, annotations) && function.isStatic() }) { func: KtNamedFunction ->
                for (annotationKClass in annotations) {
                    func.getAnnotations(binding, annotationKClass).forEach { annotation ->
                        val topicName = annotation.getValue(binding, SNSEvent::topicName)!!

                        val functionDescriptor = binding.get(BindingContext.DECLARATION_TO_DESCRIPTOR, func) as? CallableDescriptor
                        val fqName = functionDescriptor?.fqNameSafe?.asString() ?: error("Could not resolve function descriptor for ${func.name}")

                        // Split FQN like "com.example.MyClass.myMethod" into class and method
                        val lastDotIndex = fqName.lastIndexOf('.')
                        val className = if (lastDotIndex != -1) {
                            fqName.substring(0, lastDotIndex)
                        } else {
                            // Top-level function, use package from file
                            func.containingKtFile.packageFqName.asString().takeIf { it.isNotEmpty() } ?: ""
                        }
                        val method = if (lastDotIndex != -1) {
                            fqName.substring(lastDotIndex + 1)
                        } else {
                            fqName
                        }

                        val functionName = if (className.isNotEmpty()) "$className.$method" else method
                        consumersByTopic.getOrPut(topicName) { mutableListOf() }.add(functionName)
                    }
                }
            }
        }

        val handlersCode = if (consumersByTopic.isEmpty()) {
            "emptyMap()"
        } else {
            buildHandlersCode(consumersByTopic)
        }

        val fileContent =
            //language=kotlin
            """
            |package io.kotless.dsl.spring.events
            |
            |import io.kotless.dsl.cloud.aws.SNSEventData
            |
            |object SnsConsumersByTopic {
            |    val snsConsumers: Map<String, List<(SNSEventData.SNSRecord) -> Unit>> = $handlersCode
            |}
            """.trimMargin()

        return fileContent
    }

    private fun buildHandlersCode(consumersByTopic: Map<String, List<String>>): String {
        val entries = consumersByTopic.map { (topicName, functionNames) ->
            val handlers = functionNames.joinToString(",\n\t\t\t") { functionName ->
                convertToFunctionReference(functionName)
            }
            "\"$topicName\" to listOf(\n\t\t\t$handlers\n\t\t)"
        }

        return "mapOf(\n\t\t${entries.joinToString(",\n\t\t")}\n\t)"
    }

    private fun convertToFunctionReference(fullyQualifiedName: String): String {
        // Convert "com.example.MyClass.myMethod" to "com.example.MyClass::myMethod"
        val lastDotIndex = fullyQualifiedName.lastIndexOf('.')
        if (lastDotIndex == -1) {
            // No package/class separator found, return as is with ::
            return "::$fullyQualifiedName"
        }

        val className = fullyQualifiedName.substring(0, lastDotIndex)
        val methodName = fullyQualifiedName.substring(lastDotIndex + 1)
        return "$className::$methodName"
    }
}
