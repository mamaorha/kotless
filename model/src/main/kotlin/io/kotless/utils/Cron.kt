package io.kotless.utils

/**
 * Cron expression to run something each [minutes] minutes
 */
fun everyNMinutes(minutes: Int): String {
    require(minutes in 0..60) { "Cannot generate Cron expression for each N minutes, if N is $minutes (not in 0..60)" }
    return "0/$minutes * * * ? *"
}
