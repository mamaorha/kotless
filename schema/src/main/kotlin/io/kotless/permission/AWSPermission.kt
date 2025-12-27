package io.kotless.permission

import io.kotless.*

open class Permission

/**
 * Permission to act upon other resource
 *
 * It is a definition of permission to make specified actions
 * resources of specified type with specified ids.
 *
 * The permission is granted to object owning it.
 *
 * @param resource type of resource permission is for
 * @param level actions permitted by permission
 * @param ids identifiers of resources under permission
 * @param region optional custom region for the resource. If not provided, uses the default region from context
 */
data class AWSPermission(val resource: AwsResource, val level: PermissionLevel, val ids: Set<String>, val region: String? = null): Permission() {
    fun cloudIds(defaultRegion: String, account: String) = ids.map { id ->
        val effectiveRegion = region ?: defaultRegion
        // If id is "***", use it as-is (for actions that don't support resource-level permissions)
        if (id == "***") {
            "*"
        } else if (id.startsWith("arn:aws:")) {
            // If id is already a full ARN, use it as-is
            id
        } else {
            // Otherwise construct ARN from resource glob
            "${resource.glob(effectiveRegion, account)}:$id"
        }
    }.toSet()

    val actions: Set<String> = when (level) {
        PermissionLevel.Read -> resource.read
        PermissionLevel.Write -> resource.write
        PermissionLevel.ReadWrite -> resource.read + resource.write
    }
}


