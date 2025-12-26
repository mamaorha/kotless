import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

group = rootProject.group
version = rootProject.version

dependencies {
    api(kotlin("reflect"))
    api(kotlin("compiler-embeddable"))

    api(project(":schema"))

    api(project(":dsl:common:dsl-common"))
    api(project(":dsl:common:cloud:dsl-common-aws"))
    api("org.reflections:reflections:0.10.2")

    implementation(project(":dsl:kotless:cloud:kotless-lang-aws"))
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs
    }
}
