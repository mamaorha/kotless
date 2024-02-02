import io.kotless.buildsrc.Versions

group = rootProject.group
version = rootProject.version


dependencies {
    api(project(":model"))
    api(project(":dsl:common:dsl-common"))

    api(kotlin("reflect"))
    api("org.reflections", "reflections", "0.10.2")

    implementation("ch.qos.logback", "logback-classic", Versions.logback)
}

