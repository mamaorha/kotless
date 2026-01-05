import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

group = "io.kotless"
version = "0.3.5"

plugins {
    id("io.gitlab.arturbosch.detekt") version ("1.23.4") apply true
    kotlin("jvm") version "1.9.21" apply false
    `maven-publish`
}

subprojects {
    apply {
        plugin("kotlin")
        plugin("maven-publish")
        plugin("io.gitlab.arturbosch.detekt")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven(url = uri("https://packages.jetbrains.team/maven/p/ktls/maven"))
    }

    val sourceSets = this.extensions.getByName("sourceSets") as SourceSetContainer


    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"]!!.allSource)
        this.exclude("io/kotless/graal/aws/runtime/Adapter**")
    }

    publishing {
        publications {
            create<MavenPublication>("jarPublication") {
                artifactId = project.name

                // Fix for Gradle 9.x: Manually configure dependencies to avoid getDependencyProject() error
                // Instead of using from(components["java"]) which includes all dependencies,
                // we'll add the jar and manually configure only external dependencies
                artifact(tasks.named("jar"))
                artifact(tasks.named<Jar>("sourcesJar"))
                
                // Manually configure dependencies to exclude project dependencies
                // This prevents the getDependencyProject() error in Gradle 9.x
                pom {
                    withXml {
                        // Get all dependencies from the runtimeClasspath configuration
                        val runtimeClasspath = project.configurations.getByName("runtimeClasspath")
                        val externalDeps = runtimeClasspath.allDependencies.filter { 
                            it !is org.gradle.api.artifacts.ProjectDependency 
                        }
                        
                        val rootNode = asNode()
                        
                        // Remove existing dependencies node if it exists
                        val existingDeps = rootNode.get("dependencies")
                        if (existingDeps != null) {
                            if (existingDeps is groovy.util.NodeList) {
                                existingDeps.forEach { dep ->
                                    (dep as groovy.util.Node).parent().remove(dep)
                                }
                            } else if (existingDeps is groovy.util.Node) {
                                existingDeps.parent().remove(existingDeps)
                            }
                        }
                        
                        // Create new dependencies node with only external dependencies
                        if (externalDeps.isNotEmpty()) {
                            val depsNode = rootNode.appendNode("dependencies")
                            externalDeps.forEach { dep ->
                                val depNode = depsNode.appendNode("dependency")
                                depNode.appendNode("groupId", dep.group ?: "")
                                depNode.appendNode("artifactId", dep.name)
                                depNode.appendNode("version", dep.version ?: "")
                            }
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "SpacePackages"
                url = uri("https://packages.jetbrains.team/maven/p/ktls/maven")

                credentials {
                    username = System.getenv("JB_SPACE_CLIENT_ID")
                    password = System.getenv("JB_SPACE_CLIENT_SECRET")
                }
            }
        }
    }

    tasks.withType<KotlinJvmCompile> {
        kotlinOptions {
            jvmTarget = "21"
            languageVersion = "2.1"
            apiVersion = "2.1"

            freeCompilerArgs = freeCompilerArgs
        }
    }

    detekt {
        parallel = true

        config.setFrom(rootProject.files("detekt.yml"))
    }
    
    // Configure detekt reports on tasks instead of in the detekt block (Gradle 9.x compatibility)
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            xml.required.set(false)
            html.required.set(false)
        }
    }

    afterEvaluate {
        System.setProperty("gradle.publish.key", System.getenv("gradle_publish_key") ?: "")
        System.setProperty("gradle.publish.secret", System.getenv("gradle_publish_secret") ?: "")
    }
}
