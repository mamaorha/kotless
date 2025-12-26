package io.kotless

import io.kotless.gen.AWSGenerator
import io.kotless.opt.Optimizer
import io.terraformkt.terraform.TFFile
import java.io.File

object KotlessEngine {
    fun generate(schema: Schema): Set<TFFile> {
        val optimized = Optimizer.optimize(schema)
        return AWSGenerator.generate(optimized)
    }

    fun dump(genDirectory: File, files: Set<TFFile>) = files.sorted().map { file -> file.writeToDirectory(genDirectory) }
}
