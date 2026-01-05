package io.kotless.plugin.gradle.tasks.local

import io.kotless.Constants
import io.kotless.InternalAPI
import io.kotless.parser.LocalParser
import io.kotless.parser.spring.SpringBootDescriptor
import io.kotless.plugin.gradle.dsl.*
import io.kotless.plugin.gradle.utils.gradle.*
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.JavaApplication
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.dependencies
import java.io.File

/**
 * KotlessLocal task runs Kotless application locally
 *
 * @see kotless
 *
 * Note: Task is cacheable and will regenerate code only if sources or configuration has changed.
 */
@CacheableTask
internal open class KotlessLocalRunTask : DefaultTask() {

    init {
        group = Groups.kotless
    }

    @get:Input
    val myKotless: KotlessDSL
        get() = project.kotless

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val myAllSources: Set<File>
        get() = project.myKtSourceSet.toSet()

    private val finalizers = ArrayList<() -> Unit>()

    fun onShutDown(vararg finalizer: () -> Unit): KotlessLocalRunTask {
        finalizers.addAll(finalizer)
        return this
    }

    @get:Internal
    lateinit var localstack: LocalStackRunner

    @get:Internal
    var customAgent: String? = null

    @get:Internal
    var additionalEnvironment: Map<String, String> = emptyMap()

    @TaskAction
    @OptIn(InternalAPI::class)
    fun act() = with(project) {
        val dependency = Dependencies.getSpringBootDependency(project)
        require(dependency != null) { "Cannot find \"spring-boot-lang\" dependencies. It's required for local start." }

        dependencies {
            myLocal("io.kotless", SpringBootDescriptor.localLibrary, dependency.version ?: error("Explicit version is required for Kotless DSL dependency."))
        }

        val run = tasks.myGetByName<JavaExec>("run").apply {
            classpath += files(myLocal().files)

            environment[Constants.Local.serverPort] = myKotless.extensions.local.port

            val local = LocalParser.parse(myAllSources, Dependencies.getDependencies(project))
            environment[Constants.Local.Spring.classToStart] = local.entrypoint.qualifiedName.substringBefore("::")

            if (myKotless.config.optimization.autowarm.enable) {
                environment[Constants.Local.autowarmMinutes] = myKotless.config.optimization.autowarm.minutes
            }

            for ((key, value) in (myKotless.webapp.lambda.mergedEnvironment + additionalEnvironment)) {
                environment[key] = value
            }

            if (myKotless.extensions.local.useAWSEmulation) {
                environment.putAll(localstack.environment)
            }

            isIgnoreExitValue = true

            val agent = customAgent
            if (agent != null) {
                this.jvmArguments.add(agent)
            } else if (myKotless.extensions.local.debugPort != null) {
                debugOptions {
                    it.enabled.set(true)
                    it.server.set(true)
                    it.port.set(myKotless.extensions.local.debugPort)
                    it.suspend.set(myKotless.extensions.local.suspendDebug)
                }
            }
        }

        try {
            extensions.getByType(JavaApplication::class.java).mainClass.set(kotless.config.dsl.descriptor.localEntryPoint)
            run.standardInput = System.`in`
            run.exec()
        } catch (e: Throwable) {
            logger.lifecycle("Gracefully shutting down Kotless local")
            //Remove interrupted flag before execution of finalizers
            Thread.interrupted()
            finalizers.forEach { it.invoke() }
            //Rethrow exception after finalizers executed
            throw e
        }
    }
}
