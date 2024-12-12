import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":schema"))
    implementation(kotlin("reflect"))
    implementation(files("../lib/io.terraformkt/entities/0.1.5/entities-0.1.5.jar"))
    implementation(files("../lib/io.terraformkt.providers/aws/3.14.1-0.1.4/aws-3.14.1-0.1.4.jar"))
    implementation(files("../lib/io.terraformkt.providers/azure/2.78.0-0.1.5/azure-2.78.0-0.1.5.jar"))
}


tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs
    }
}
