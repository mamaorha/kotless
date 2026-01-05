package io.kotless.plugin.gradle.dsl

import io.kotless.plugin.gradle.utils.gradle.myShadowJarName
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.io.File
import java.io.Serializable

/** Configuration of Kotless itself */
@KotlessDSLTag
class KotlessGradleConfig(project: Project) : Serializable {
    /**
     * A local directory Kotless will use to store needed binaries (like terraform)
     * By default it is `${buildDir}/kotless-bin`
     */
    var binDirectory = File(project.buildDir, "kotless-bin")

    /**
     * A local directory Kotless will use to store generated files
     * By default it is `${buildDir}/kotless-gen`
     */
    var genDirectory = File(project.buildDir, "kotless-gen")


    internal val deployGenDirectory: File
        get() = File(genDirectory, "deploy")

    internal val localGenDirectory: File
        get() = File(genDirectory, "local")

    /** Name of configuration to use as a classpath */
    var configurationName = "compileClasspath"

    internal var myArchiveTask: String = project.myShadowJarName()

    /** Set custom archive task that should be used to pack lambda instead of default ShadowJar */
    fun setArchiveTask(task: AbstractArchiveTask) {
        myArchiveTask = task.name
    }

    @KotlessDSLTag
    class DSLGradle(project: Project) : Serializable {
        /** Descriptor for Spring Boot DSL */
        internal val descriptor: io.kotless.parser.DSLDescriptor
            get() = io.kotless.parser.spring.SpringBootDescriptor

        /** Statics root correctly resolved for DSL */
        internal val resolvedStaticsRoot
            get() = staticsRoot

        /** Working directory of current project */
        private val workingRoot: File = project.projectDir

        /**
         * Directory Kotless considers as root for Static Resources resolving
         *
         * Will be used for Kotless DSL and SpringBoot to search for static resources.
         *
         * By default, it is `src/main/resources`
         */
        var staticsRoot: File = project.projectDir.resolve("src/main/resources")
    }

    internal val dsl: DSLGradle = DSLGradle(project)

    /** Configuration of DSL used by Kotless */
    @KotlessDSLTag
    fun dsl(configure: DSLGradle.() -> Unit) {
        dsl.configure()
    }

    sealed class CloudGradle<S : CloudGradle.StorageGradle, T : CloudGradle.TerraformGradle<*, *>> : Serializable {

        /** Prefix with which all created resources will be prepended */
        var prefix: String = ""

        class AWS : CloudGradle<StorageGradle.S3, TerraformGradle.AWS>() {
            lateinit var profile: String
            lateinit var region: String
        }

        @KotlessDSLTag
        sealed class StorageGradle: Serializable {
            class S3 : StorageGradle() {
                lateinit var bucket: String
                var region: String? = null
            }
        }

        internal val storage: S = StorageGradle.S3() as S

        fun storage(configure: S.() -> Unit) {
            storage.configure()
        }


        @KotlessDSLTag
        sealed class TerraformGradle<B : TerraformGradle.BackendGradle<*>, P : TerraformGradle.ProviderGradle>(
            internal val backend: B, internal val provider: P
        ) : Serializable {

            class AWS(backend: BackendGradle.AWS, provider: ProviderGradle.AWS) : TerraformGradle<BackendGradle.AWS, ProviderGradle.AWS>(backend, provider)

            /**
             * Version of Terraform to use.
             * By default, `1.8.2`
             */
            var version: String = "1.8.2"

                sealed class BackendGradle<S : StorageGradle> : Serializable {
                @KotlessDSLTag
                class AWS : BackendGradle<StorageGradle.S3>() {
                    internal var s3: StorageGradle.S3? = null

                    fun s3(configure: StorageGradle.S3.() -> Unit) {
                        s3 = StorageGradle.S3().also(configure)
                    }

                    /**
                     * Path in a bucket to store Terraform state
                     * By default it is `kotless-state/state.tfstate`
                     */
                    var key: String = "kotless-state/state.tfstate"

                    var profile: String? = null
                }
            }

            /** Configuration of Terraform backend */
            @KotlessDSLTag
            fun backend(configure: B.() -> Unit) {
                backend.configure()
            }

            sealed class ProviderGradle : Serializable {
                @KotlessDSLTag
                class AWS : ProviderGradle() {
                    /** Version of AWS provider to use */
                    var version = "5.30.0"

                    var profile: String? = null

                    var region: String? = null
                }
            }

            /** Configuration of Terraform AWS provider */
            @KotlessDSLTag
            fun provider(configure: P.() -> Unit) {
                provider.configure()
            }
        }

        internal val terraform = TerraformGradle.AWS(TerraformGradle.BackendGradle.AWS(), TerraformGradle.ProviderGradle.AWS()) as T

        @KotlessDSLTag
        fun terraform(configure: T.() -> Unit) {
            terraform.configure()
        }
    }


    var cloud: CloudGradle<*, *>? = null

    @KotlessDSLTag
    fun aws(configure: CloudGradle.AWS.() -> Unit) {
        cloud = CloudGradle.AWS().also(configure)
    }


    @KotlessDSLTag
    class Optimization : Serializable {
        /**
         * Optimization defines, if different lambdas should be merged into one and when.
         *
         * Basically, lambda serving few endpoints is more likely to be warm.
         *
         * There are 3 levels of merge optimization:
         * * None -- lambdas will never be merged
         * * PerPermissions -- lambdas will be merged, if they have equal permissions
         * * All -- all lambdas in context are merged in one
         */
        enum class MergeLambda {
            None,
            PerPermissions,
            All
        }
        
        var mergeLambda: MergeLambda = MergeLambda.All

        /**
         * Optimization defines, if lambdas should be autowarmed and with what schedule
         *
         * Lambdas cannot be autowarmed with interval more than hour, since it has no practical sense
         */
        @KotlessDSLTag
        data class Autowarm(val enable: Boolean, val minutes: Int = 5) : Serializable

        var autowarm: Autowarm = Autowarm(enable = true)
    }

    internal val optimization: Optimization = Optimization()

    /** Optimizations applied during generation */
    @KotlessDSLTag
    fun optimization(configure: Optimization.() -> Unit) {
        optimization.configure()
    }
}
