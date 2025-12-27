package io.kotless.plugin.gradle.tasks.gen

import io.kotless.InternalAPI
import io.kotless.plugin.gradle.dsl.KotlessDSL
import io.kotless.plugin.gradle.dsl.kotless
import io.kotless.plugin.gradle.spring.resources.SnsConsumersByTopicSource
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

@CacheableTask
@OptIn(InternalAPI::class)
internal open class KotlessClassOverrideTask : DefaultTask() {
    @get:OutputDirectory
    val kotlinOverridePath: File
        get() = File(project.buildDir, "override/sources/kotless/")

    @TaskAction
    fun act() {
        generateSnsConsumersFile(project.kotless)
    }

    private fun generateSnsConsumersFile(kotlessDsl: KotlessDSL) {
        val file = File(kotlinOverridePath, SnsConsumersByTopicSource.filePath)
        file.parentFile.mkdirs()

        file.writeText(SnsConsumersByTopicSource.data(project))
    }
}
