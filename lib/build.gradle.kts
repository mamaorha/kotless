plugins {
    id("maven-publish")
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenLocal()  // Ensures we publish to local Maven
}

publishing {
    publications {
        fileTree("repo") {
            include("**/*.jar")
        }.files.forEach { jarFile ->
            val jarGroupId = jarFile.parentFile.parentFile.parentFile.name
            val jarArtifactId = jarFile.parentFile.parentFile.name
            val jarVersion = jarFile.parentFile.name

            val libName = jarFile.name.removeSuffix(".jar")

            create<MavenPublication>(libName) {
                this.groupId = jarGroupId
                this.artifactId = jarArtifactId
                this.version = jarVersion
                artifact(jarFile) {
                    if(jarFile.name.endsWith("-sources.jar")) {
                        classifier = "sources"
                    }
                }
            }
        }
    }
}
