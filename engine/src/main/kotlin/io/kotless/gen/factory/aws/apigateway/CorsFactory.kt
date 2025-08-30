package io.kotless.gen.factory.aws.apigateway

import io.kotless.Application
import io.kotless.gen.GenerationContext
import io.kotless.gen.GenerationFactory
import io.kotless.gen.factory.aws.route.AbstractRouteFactory
import io.kotless.terraform.functions.link
import io.terraformkt.aws.resource.apigateway.api_gateway_method
import io.terraformkt.aws.resource.apigateway.api_gateway_integration
import io.terraformkt.aws.resource.apigateway.api_gateway_method_response
import io.terraformkt.aws.resource.apigateway.api_gateway_integration_response

object CorsFactory : GenerationFactory<Application.API, CorsFactory.Output> {
    data class Output(val corsMethods: List<String>, val corsIntegrations: List<String>)

    override fun mayRun(entity: Application.API, context: GenerationContext) = entity.allowCors && context.output.check(entity, RestAPIFactory)

    override fun generate(entity: Application.API, context: GenerationContext): GenerationFactory.GenerationResult<Output> {
        val api = context.output.get(entity, RestAPIFactory)
        
        // Get all unique resource paths that need CORS OPTIONS methods
        val resourcePaths = mutableSetOf<io.kotless.URIPath>()
        
        // Add root path
        resourcePaths.add(io.kotless.URIPath())
        
        // Add all dynamic route paths
        entity.dynamics.forEach { route ->
            resourcePaths.add(route.path)
        }
        
        // Add all static route paths
        entity.statics.forEach { route ->
            resourcePaths.add(route.path)
        }

        val corsMethods = mutableListOf<String>()
        val corsIntegrations = mutableListOf<String>()
        val allGeneratedResources = mutableListOf<io.terraformkt.hcl.HCLEntity.Named>()

        // Create OPTIONS method for each resource path
        resourcePaths.forEach { path ->
            val resourceApi = AbstractRouteFactory().getResource(path, api, context)
            
            // Create OPTIONS method for this resource
            val corsMethod = api_gateway_method(context.names.tf(entity.name, "cors", "options", path.parts.joinToString("_").ifBlank { "root" })) {
                rest_api_id = api.rest_api_id
                resource_id = resourceApi.id
                http_method = "OPTIONS"
                authorization = "NONE"
            }

            // Create CORS integration (mock integration) for this resource
            val corsIntegration = api_gateway_integration(context.names.tf(entity.name, "cors", "options", path.parts.joinToString("_").ifBlank { "root" })) {
                depends_on = arrayOf(link(corsMethod.hcl_ref))
                rest_api_id = api.rest_api_id
                resource_id = resourceApi.id
                http_method = "OPTIONS"
                type = "MOCK"
                requestTemplates(mapOf("application/json" to "{\\\"statusCode\\\": 200}"))
            }

            // Create method response for CORS on this resource
            val corsMethodResponse = api_gateway_method_response(context.names.tf(entity.name, "cors", "options", "response", path.parts.joinToString("_").ifBlank { "root" })) {
                depends_on = arrayOf(link(corsMethod.hcl_ref))
                rest_api_id = api.rest_api_id
                resource_id = resourceApi.id
                http_method = "OPTIONS"
                status_code = "200"
                responseParameters(
                    mapOf(
                        "method.response.header.Access-Control-Allow-Headers" to true,
                        "method.response.header.Access-Control-Allow-Methods" to true,
                        "method.response.header.Access-Control-Allow-Origin" to true,
                        "method.response.header.Access-Control-Max-Age" to true
                    )
                )
            }

            // Create integration response for CORS on this resource
            val corsIntegrationResponse = api_gateway_integration_response(context.names.tf(entity.name, "cors", "options", "response", path.parts.joinToString("_").ifBlank { "root" })) {
                depends_on = arrayOf(link(corsMethodResponse.hcl_ref), link(corsIntegration.hcl_ref))
                rest_api_id = api.rest_api_id
                resource_id = resourceApi.id
                http_method = "OPTIONS"
                status_code = "200"
                responseParameters(
                    mapOf(
                        "method.response.header.Access-Control-Allow-Headers" to "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'",
                        "method.response.header.Access-Control-Allow-Methods" to "'GET,POST,PUT,DELETE,OPTIONS'",
                        "method.response.header.Access-Control-Allow-Origin" to "'*'",
                        "method.response.header.Access-Control-Max-Age" to "'86400'"
                    )
                )
            }

            corsMethods.add(corsMethod.hcl_ref)
            corsIntegrations.add(corsIntegration.hcl_ref)
            
            allGeneratedResources.addAll(listOf(corsMethod, corsIntegration, corsMethodResponse, corsIntegrationResponse))
        }

        return GenerationFactory.GenerationResult(
            Output(corsMethods, corsIntegrations),
            *allGeneratedResources.toTypedArray()
        )
    }
}
