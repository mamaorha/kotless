import io.kotless.plugin.gradle.dsl.kotless

group = rootProject.group
version = rootProject.version

plugins {
    id("io.kotless") version "0.3.3" apply true
}

dependencies {
    implementation("io.kotless", "kotless-lang-aws", "0.3.3")
    implementation("io.kotless", "kotless-lang", "0.3.3")

    implementation(project(":common:site-shared"))
}

kotless {
    config {
        aws {
            prefix = "site"

            storage {
                bucket = "eu.site.s3.ktls.aws.intellij.net"
            }

            profile = "kotless-jetbrains"
            region = "eu-west-1"
        }
    }

    webapp {
        dns("site", "kotless.io")
    }
}

