import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

group = "io.kotless"
version = "0.3.5"

plugins {
    id("io.gitlab.arturbosch.detekt") version ("1.23.4") apply true
    kotlin("jvm") version "2.3.0" apply false
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
        from(sourceSets["main"].allSource)
        this.exclude("io/kotless/graal/aws/runtime/Adapter**")
    }

    publishing {
        publications {
            create<MavenPublication>("jarPublication") {
                artifactId = project.name

                artifact(tasks.named("jar"))
                artifact(tasks.named<Jar>("sourcesJar"))
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
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
            languageVersion.set(KotlinVersion.KOTLIN_2_3)
            apiVersion.set(KotlinVersion.KOTLIN_2_3)
        }
    }

    detekt {
        parallel = true

        config.setFrom(rootProject.files("detekt.yml"))
    }
    
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
