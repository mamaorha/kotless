package io.kotless.plugin.gradle.dsl

import java.io.File
import java.io.Serializable

/** Extensions for Kotless deployment pipeline */
@KotlessDSLTag
class Extensions : Serializable {
    @KotlessDSLTag
    class Terraform : Serializable {
        /** Allow usage of Destroy task */
        var allowDestroy = false

        @KotlessDSLTag
        class Files : Serializable {
            internal val additional = HashSet<File>()

            /** Add files to Terraform code generated by Kotless */
            fun add(file: File) {
                additional.add(file)
            }
        }

        internal val files = Files()

        /** Files to add to Terraform code generated by Kotless */
        @KotlessDSLTag
        fun files(configure: Files.() -> Unit) {
            files.configure()
        }
    }

    internal val terraform = Terraform()

    /** Extensions to terraform generation and execution */
    @KotlessDSLTag
    fun terraform(configure: Terraform.() -> Unit) {
        terraform.configure()
    }

    @KotlessDSLTag
    class Local : Serializable {
        /** Port to use for local run of Kotless */
        var port: Int = 8080

        /** Run AWS Mock for local development */
        var useAWSEmulation: Boolean = false

        var debugPort: Int? = null
        var suspendDebug: Boolean = false
    }

    internal val local = Local()

    /**
     * Configuration of local Kotless execution.
     * Applicable only for Ktor DSL
     */
    @KotlessDSLTag
    fun local(configure: Local.() -> Unit) {
        local.configure()
    }
}
