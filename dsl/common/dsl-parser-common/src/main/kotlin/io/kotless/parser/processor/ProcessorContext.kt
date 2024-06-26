package io.kotless.parser.processor

import io.kotless.*
import io.kotless.resource.Lambda
import io.kotless.resource.StaticResource
import io.kotless.utils.TypedStorage
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.io.File
import java.net.URLClassLoader

/**
 * Context of code analysis.
 *
 * It includes resulting elements of Kotless Schema, set of already ran processors
 * and part of schema explicitly defined by user
 */
@OptIn(InternalAPI::class)
class ProcessorContext(val jar: File, val config: KotlessConfig, val lambda: Lambda.Config, libs: Set<File>) {
    class Output(private val outputs: MutableMap<Processor<*>, Any> = HashMap()) {
        fun <T : Any> register(processor: Processor<T>, output: T) {
            outputs[processor] = output
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> get(processor: Processor<T>): T {
            return outputs[processor] as T
        }

        fun <T : Any> check(processor: Processor<T>) = processor in outputs
    }

    val output = Output()

    class Resources(val dynamics: TypedStorage<Lambda> = TypedStorage(), val statics: TypedStorage<StaticResource> = TypedStorage()) {
        fun register(key: TypedStorage.Key<Lambda>, lambda: Lambda) {
            dynamics[key] = lambda
        }

        fun register(key: TypedStorage.Key<StaticResource>, static: StaticResource) {
            statics[key] = static
        }
    }

    val resources = Resources()

    class Routes(
        private val myDynamics: MutableSet<Application.API.DynamicRoute> = HashSet(),
        private val myStatics: MutableSet<Application.API.StaticRoute> = HashSet()
    ) {
        val dynamics: Set<Application.API.DynamicRoute>
            get() = myDynamics.toSet()

        val statics: Set<Application.API.StaticRoute>
            get() = myStatics.toSet()


        fun register(dynamic: Application.API.DynamicRoute) {
            myDynamics.add(dynamic)
        }

        fun register(static: Application.API.StaticRoute) {
            myStatics.add(static)
        }
    }

    val routes = Routes()

    class Events(private val myScheduled: MutableSet<Application.Events.Scheduled> = HashSet()) {
        val scheduled: Set<Application.Events.Scheduled>
            get() = myScheduled.toSet()

        fun register(scheduled: Application.Events.Scheduled) {
            myScheduled.add(scheduled)
        }
    }

    val events = Events()

    private val fullUrls by lazy {
        libs.map { it.toURI().toURL() }.toSet()
    }

    val libsClassLoader: ClassLoader by lazy {
        URLClassLoader.newInstance(
            fullUrls.toTypedArray(),
            javaClass.classLoader
        )
    }

    val reflections by lazy {
        val configuration = ConfigurationBuilder()
            .setScanners(Scanners.TypesAnnotated, Scanners.SubTypes)
            .addClassLoaders(libsClassLoader)
            .addUrls(fullUrls)

        Reflections(configuration)
    }
}
