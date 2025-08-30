# CORS Support in Kotless

Kotless supports automatic CORS (Cross-Origin Resource Sharing) configuration for API Gateway through a structured CORS configuration block.

## What is CORS?

CORS (Cross-Origin Resource Sharing) is a security feature implemented by web browsers that controls how web pages in one domain can request and interact with resources from another domain. When enabled, it allows your API to be accessed from web applications running on different domains.

## How to Enable CORS

To enable CORS for your Kotless application, use the CORS configuration block:

### CORS Configuration Structure

The new CORS configuration provides a more structured approach to CORS settings:

```kotlin
webapp {
    cors {
        enabled = true  // Enable/disable CORS
        // Future: Additional CORS options can be added here
        // allowedOrigins = listOf("https://example.com", "https://app.example.com")
        // allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
        // allowedHeaders = listOf("Content-Type", "Authorization")
    }
}
```

This structure allows for future expansion of CORS configuration options.

```kotlin
kotless {
    config {
        aws {
            prefix = "myapp"
            profile = "my-profile"
            region = "us-east-1"
        }
    }

    webapp {
        dns("myapp", "example.com")
        cors {
            enabled = true  // Enable CORS for this API Gateway
        }
    }
}
```

## What Gets Generated

When CORS is enabled through `cors { enabled = true }`, Kotless automatically generates the following Terraform resources:

### 1. OPTIONS Methods for Preflight Requests
- Creates OPTIONS methods on **all resources** that have other HTTP methods
- Handles CORS preflight requests from browsers for any path
- Returns appropriate CORS headers

### 2. CORS Headers on All Routes
- Adds CORS headers to all dynamic and static route responses
- Includes:
  - `Access-Control-Allow-Origin: *`
  - `Access-Control-Allow-Headers: Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token`
  - `Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS`

### 3. Proper Dependencies
- Ensures CORS resources are created before deployment
- Maintains correct resource creation order

## Generated Terraform Resources

The following Terraform resources are automatically created for **each resource path** that has HTTP methods:

```hcl
# OPTIONS method for CORS preflight on each resource
resource "aws_api_gateway_method" "myapp_cors_options_resource_path" {
  rest_api_id   = aws_api_gateway_rest_api.myapp.id
  resource_id   = aws_api_gateway_resource.resource_path.id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

# CORS integration (mock) for each resource
resource "aws_api_gateway_integration" "myapp_cors_options_resource_path" {
  rest_api_id = aws_api_gateway_rest_api.myapp.id
  resource_id = aws_api_gateway_resource.resource_path.id
  http_method = "OPTIONS"
  type        = "MOCK"
  request_templates = {
    "application/json" = "{\"statusCode\": 200}"
  }
}

# CORS method response for each resource
resource "aws_api_gateway_method_response" "myapp_cors_options_response_resource_path" {
  rest_api_id = aws_api_gateway_rest_api.myapp.id
  resource_id = aws_api_gateway_resource.resource_path.id
  http_method = "OPTIONS"
  status_code = "200"
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = true
    "method.response.header.Access-Control-Allow-Methods" = true
    "method.response.header.Access-Control-Allow-Origin"  = true
    "method.response.header.Access-Control-Max-Age"       = true
  }
}

# CORS integration response for each resource
resource "aws_api_gateway_integration_response" "myapp_cors_options_response_resource_path" {
  rest_api_id = aws_api_gateway_rest_api.myapp.id
  resource_id = aws_api_gateway_resource.resource_path.id
  http_method = "OPTIONS"
  status_code = "200"
  response_parameters = {
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
    "method.response.header.Access-Control-Allow-Methods" = "'GET,POST,PUT,DELETE,OPTIONS'"
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
    "method.response.header.Access-Control-Max-Age"       = "'86400'"
  }
}
```

**Note**: The above resources are created for each unique resource path in your API, ensuring that CORS preflight requests work for all endpoints, not just the root path.

## Security Considerations

- **Wildcard Origin**: The current implementation uses `Access-Control-Allow-Origin: *`, which allows any domain to access your API. In production, consider restricting this to specific domains.
- **Headers**: The allowed headers include common AWS and API Gateway headers. You can customize these by modifying the `CorsFactory`.
- **Methods**: All common HTTP methods are allowed. Adjust based on your API's requirements.

## Customization

To customize the CORS configuration, you can modify the `CorsFactory` in `engine/src/main/kotlin/io/kotless/gen/factory/aws/apigateway/CorsFactory.kt`:

- Change allowed origins
- Modify allowed headers
- Adjust allowed methods
- Set custom max-age values

## Example Use Cases

1. **Frontend Applications**: Allow your React/Vue/Angular app to call your API
2. **Mobile Apps**: Enable mobile applications to access your API
3. **Third-party Integrations**: Allow external services to integrate with your API
4. **Development**: Test your API from different localhost ports

## Migration

The `cors.enabled` property defaults to `false`, so no CORS resources will be generated unless explicitly enabled.

## Troubleshooting

If you encounter CORS issues:

1. Ensure CORS is enabled in your configuration with `cors { enabled = true }`
2. Check that the deployment completed successfully
3. Verify that OPTIONS methods exist in API Gateway for all your API endpoints
4. Check browser developer tools for CORS error messages
5. Ensure your client is making the request from an allowed origin

**Enhanced CORS Support**: The current implementation creates OPTIONS methods on all resources, not just the root. This ensures that preflight requests work for any endpoint in your API, providing comprehensive CORS support.
