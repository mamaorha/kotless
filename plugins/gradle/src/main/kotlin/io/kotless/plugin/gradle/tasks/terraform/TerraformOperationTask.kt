package io.kotless.plugin.gradle.tasks.terraform

import io.kotless.plugin.gradle.dsl.kotless
import io.kotless.plugin.gradle.utils.CommandLine
import io.ktor.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.*
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.*
import kotlin.io.path.isRegularFile


/**
 * TerraformOperation task executes specified operation on generated terraform code
 *
 * It takes all the configuration from global KotlessDSL configuration (from `kotless` field)
 * and more precisely -- `genDirectory` from it's `kotlessConfig` field.
 *
 * @see kotless
 *
 * Note: Apply will not require approve in a console, kotless passes to it `-auto-approve`
 */
internal open class TerraformOperationTask : DefaultTask() {
    init {
        outputs.upToDateWhen { false }
    }

    enum class Operation(val op: List<String>) {
        INIT(listOf("init")),
        PLAN(listOf("plan")),
        APPLY(listOf("apply", "-auto-approve")),
        DESTROY(listOf("destroy", "-auto-approve"));
    }

    @get:Input
    lateinit var operation: Operation

    @get:InputDirectory
    lateinit var root: File

    @get:Input
    var environment: Map<String, String> = emptyMap()

    @TaskAction
    fun act() {
        convertDosToUnix(project.projectDir.toPath())

        CommandLine.executeOrFail(
            exec = TerraformDownloadTask.tfBin(project).absolutePath,
            args = operation.op,
            envs = environment,
            workingDir = root,
            redirectStdout = true,
            redirectErr = true
        )
    }

    private fun convertDosToUnix(folderPath: Path) {
        try {
            Files.walk(folderPath)
                .filter { it != null }
                .filter { path: Path -> Files.isRegularFile(path) }
                .forEach { file: Path ->
                    try {
                        convertFileDosToUnix(file)
                    } catch (e: IOException) {
                        logger.error("Failed to convert file: $file", e)
                    }
                }

            logger.lifecycle("Dos2Unix Conversion completed successfully.")
        } catch (e: IOException) {
            logger.error("Failed to walk through folder: $folderPath", e)
        }
    }

    private fun convertFileDosToUnix(file: Path) {
        val extension by lazy { file.extension.lowercase() }

        if (file.isRegularFile() && (extension == "kt" || extension == "java")) {
            val fileBytes: ByteArray = Files.readAllBytes(file)
            var fileContent = String(fileBytes, StandardCharsets.UTF_8)
            fileContent = fileContent.replace("\r\n".toRegex(), "\n") // Convert DOS to Unix
            Files.write(file, fileContent.toByteArray(StandardCharsets.UTF_8))
            logger.lifecycle("Converted file: $file")
        }
    }
}
