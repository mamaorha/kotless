package io.kotless.gen.factory.apigateway

import io.kotless.Webapp
import io.kotless.gen.*
import io.kotless.hcl.HCLEntity
import io.kotless.hcl.ref
import io.kotless.terraform.functions.eval
import io.kotless.terraform.functions.timestamp
import io.kotless.terraform.provider.aws.resource.apigateway.api_gateway_deployment

object DeploymentFactory : GenerationFactory<Webapp.ApiGateway.Deployment, DeploymentFactory.DeploymentOutput> {
    data class DeploymentOutput(val stage_name: String)

    override fun mayRun(entity: Webapp.ApiGateway.Deployment, context: GenerationContext) = context.check(context.webapp.api, RestAPIFactory)

    override fun generate(entity: Webapp.ApiGateway.Deployment, context: GenerationContext): GenerationFactory.GenerationResult<DeploymentOutput> {
        val api = context.get(context.webapp.api, RestAPIFactory)

        val deployment = api_gateway_deployment(Names.tf(entity.name)) {
            rest_api_id = api.rest_api_id
            stage_name = entity.version

            variables = object : HCLEntity() {
                val deployed_at by text(default = eval(timestamp()))
            }
        }

        return GenerationFactory.GenerationResult(DeploymentOutput(deployment::stage_name.ref), deployment)
    }
}