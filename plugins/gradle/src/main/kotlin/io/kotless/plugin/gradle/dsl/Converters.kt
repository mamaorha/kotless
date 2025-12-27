package io.kotless.plugin.gradle.dsl

import io.kotless.CloudPlatform
import io.kotless.KotlessConfig

internal fun KotlessDSL.toSchema(): KotlessConfig {
    return with(config) {
        KotlessConfig(
            cloud!!.toSchema(),
            KotlessConfig.DSL(dsl.typeOrDefault, dsl.resolvedStaticsRoot),
            KotlessConfig.Optimization(
                optimization.mergeLambda,
                KotlessConfig.Optimization.AutoWarm(optimization.autowarm.enable, optimization.autowarm.minutes)
            )
        )
    }
}

internal fun KotlessGradleConfig.CloudGradle<*, *>.toSchema(): KotlessConfig.Cloud<*, *> {
    return (this as KotlessGradleConfig.CloudGradle.AWS).toSchema()
}

internal fun KotlessGradleConfig.CloudGradle.AWS.toSchema(): KotlessConfig.Cloud<*, *> {
    return KotlessConfig.Cloud.AWS(
        prefix,
        KotlessConfig.Cloud.Storage.S3(
            storage.bucket,
            storage.region ?: region
        ),
        KotlessConfig.Cloud.Terraform.AWS(
            terraform.version,
            KotlessConfig.Cloud.Terraform.Backend.AWS(
                KotlessConfig.Cloud.Storage.S3(
                    terraform.backend.s3?.bucket ?: storage.bucket,
                    terraform.backend.s3?.region ?: storage.region ?: region
                ),
                terraform.backend.key,
                terraform.backend.profile ?: profile,
            ),
            KotlessConfig.Cloud.Terraform.Provider.AWS(
                terraform.provider.version,
                terraform.provider.profile ?: profile,
                terraform.provider.region ?: region
            )
        )
    )
}

internal fun Webapp.DNS.toSchema(): io.kotless.Application.DNS = io.kotless.Application.DNS(zone, alias, certificate)
internal fun Webapp.Deployment.toSchema(path: String): io.kotless.Application.API.Deployment = io.kotless.Application.API.Deployment(
    name ?: path.trim(':').let { if (it.isBlank()) "root" else it.replace(':', '_') },
    version
)
