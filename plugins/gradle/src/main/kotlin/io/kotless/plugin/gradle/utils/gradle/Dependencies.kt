package io.kotless.plugin.gradle.utils.gradle

import io.kotless.parser.spring.SpringBootDescriptor
import io.kotless.plugin.gradle.dsl.kotless
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import java.io.File

internal object Dependencies {
    fun getSpringBootDependency(project: Project): Dependency? {
        return getDependency(project, SpringBootDescriptor.apiLibrary, getConfigurationName(project))
    }

    fun getDependencies(project: Project): Set<File> {
        return getConfiguration(project, getConfigurationName(project)).files.toSet()
    }

    private fun getConfigurationName(project: Project): String {
        // Try to get from kotless config if available, otherwise use default
        return try {
            project.kotless.config.configurationName
        } catch (e: Exception) {
            "compileClasspath" // Default value
        }
    }

    private fun getConfiguration(project: Project, configurationName: String = getConfigurationName(project)): Configuration {
        return project.configurations.getByName(configurationName)
    }

    private fun getDependency(project: Project, name: String, configurationName: String = getConfigurationName(project)): Dependency? {
        val depsConfiguration = getConfiguration(project, configurationName)
        val deps = depsConfiguration.allDependencies

        return deps.find { it.group == "io.kotless" && it.name == name }
    }
}

