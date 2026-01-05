# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)

# 0.3.5 - 2025-01-XX

## Changed

* Updated Kotlin to 2.3.0 (required for Java 25 support)
* Updated Spring Boot to 3.5.9
* Updated GraalVM to 25
* Updated Gradle to 9.2.1 (required for GraalVM 25/Java 25 support)
* Added Java 25 runtime support for AWS Lambda execution

## Breaking Changes

This release introduces several breaking changes that require action before upgrading:

### Gradle 9.2.1+ Required

This version requires **Gradle 9.2.1 or later** to support Java 25 and GraalVM 25. 

**Action required:** Update your Gradle wrapper to version 9.2.1 or later.

**1. Update the Gradle wrapper version in `gradle/wrapper/gradle-wrapper.properties`:**

```properties
# Change from:
distributionUrl=https\://services.gradle.org/distributions/gradle-8.x-bin.zip

# To:
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
```

**2. Alternatively, use the Gradle wrapper command to update:**

```bash
./gradlew wrapper --gradle-version=9.2.1
```

Or on Windows:
```powershell
.\gradlew wrapper --gradle-version=9.2.1
```

**3. Verify the update:**

```bash
./gradlew --version
```

This should show Gradle 9.2.1 or later.

### Java 25 Runtime Required

This version now uses **Java 25 runtime** for AWS Lambda execution. 

**Action required:** 
- Install Java 25 or later JDK for development
- Ensure your CI/CD pipelines use Java 25 or later

### GraalVM 25 Required

This version now uses **GraalVM 25** for GraalVM native image deployments.

**Action required:** Install GraalVM 25 if you use GraalVM native image builds.

### Kotlin 2.3.0 Required

This version requires **Kotlin 2.3.0**. Update your build configuration as follows:

**1. Update Kotlin plugin version in your root `build.gradle.kts`:**

```kotlin
// Change from:
kotlin("jvm") version "1.9.21" apply false

// To:
kotlin("jvm") version "2.3.0" apply false
```

**2. Update Kotlin compiler options in your `build.gradle.kts`:**

```kotlin
// Change from:
tasks.withType<KotlinJvmCompile> {
    kotlinOptions {
        jvmTarget = "21"
        languageVersion = "2.1"
        apiVersion = "2.1"
    }
}

// Change to:
tasks.withType<KotlinJvmCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        languageVersion.set(KotlinVersion.KOTLIN_2_3)
        apiVersion.set(KotlinVersion.KOTLIN_2_3)
    }
}
```


# 0.3.4 - 2025-12-26

* Breaking change: you should remove dependency in "kotless-lang-aws" as its no longer part of the project
* removed ktor & kotless dsl
* removed azure support
* support sns
* support gamelift permission
* adjusted examples

# 0.3.3 - 2023-12-25

## Changed

* Support graal for spring-boot
* Support lambda VPC

# 0.3.1 - 2023-12-16

## Changed

* Support java 21

# 0.3.0 - 2023-12-16

## Changed

* Support spring-boot 3

# 0.2.3 - 2023-12-16

## Changed

* Upgrade dependencies and support higher spring-boot

# 0.2.2 - 2023-12-16

## Added

* Support remote debug of local kotless via extension args - debugPort, suspendDebug

# 0.2.1 - 2023-12-16

## Changed

* Support java 17

# 0.2.0 - 2021-10-19

* Introduce support of Azure cloud 
* Migrate from JCenter to Space Packages
* A lot of changes inside the platform

# 0.1.7-beta-5 - 2021-02-01

## Changed

* Migrated to Gradle 6.8.1 with Kotlin 1.4.21
* Updated Ktor version to 1.5.0
* Updated Spring Boot version to 2.4.2

## Fixed

* Now Kotless correctly handles favicons thanks to mkuzmin

# 0.1.7-beta-4 - 2020-10-09

## Added

* Initial GraalVM support
    * Works only with Ktor right now
    * Ktor Site examples migrated to GraalVM

## Fixed

* Now Kotless should be working on Windows, thanks to zaenk (chmod will not be used on Windows)

# 0.1.6 - 2020-08-25

## Added

* Support for different runtimes: Java 8 and Java 11

## Changed

* Migrate to Terraform 12 by default

## Fixed

* Problem with LocalStack not stopping after a run

# 0.1.5 - 2020-06-02

## Fixed

* Reflections dependency reverted to older version because of critical bug in it
* Fix for HTTPRequest -- sometimes user-agent can miss in APIGateway request

# 0.1.4 - 2020-05-31

## Added

* Spring Boot DSL -- Spring Boot serverless container and parser of it. Support dynamic and static
  routes, warming of lambda, granular permissions. Does not support Scheduled.
* Support local run for Spring Boot DSL via tomcat starter
* Spring Boot examples: shortener and site

## Changed

* Examples were reworked into one project
* `workDirectory` is now called `staticsRoot`

## Fixed

* Improvements in all parsers -- now all of them should work a lot faster
* Fixes to documentation in code

# 0.1.3 - 2020-02-08

## Added

* Output to console URL of deployed application
* Support local start for Kotless DSL
* Support @Scheduled execution for local starts
* Use AWS Local Stack for mocking of AWS services during local start
    * Extension files will be automatically applied to LocalStack instance

## Fixed

* Support deployment without Route53 record -- will use generated by API Gateway DNS record. Note:
  *Usage of generated record may lead to problems with hardcoded links. Kotless Links API works with
  them correctly.*
* Support headless mode -- without any configuration Gradle project should be successfully imported and
  local starts will work. Still, configuration is required for actual deployment.

# 0.1.2 - 2019-11-03

## Added

* Ktor DSL -- Ktor Engine and parser for it. Support dynamic and static routes, warming of lambda,
  granular permissions. Does not support Scheduled.
* Add local run task for Ktor DSL -- now you can run server locally.
* Support of all remaining HTTP methods in Kotless and Ktor DSL
* Ktor examples: add shortener and site

# 0.1.1 - 2019-10-14

## Added

* Support of binary responses for binary MimeTypes
* Scheduled events -- just annotate function with @Scheduled
* Extensions API -- now it is possible to use custom Terraform code along with Kotless generated
  during deployment.
* URL shortener example -- simple URL shortener written with Kotless

## Changed

* Separate Terraform synthesizing into Terraform DSL, Generators and Optimizers
* Minor style changes in Gradle DSL

## Fixed

* Multiregional -- now Kotless can be deployed to any region
* Default parameters in functions now back to working
* Format of S3 resource arn in permissions
* Deploy-time check of signatures of annotated functions

# 0.1.0 — 2019-06-18

### Added

* Explicitly declared permissions, e.g. `@S3Bucket(bucket = "my_bucket", mode = Mode.Read`
    * Works for functions, classes and objects
    * Taken from routes and global actions (like `LambdaWarming`, `LambdaInit` and so on)
* `LambdaWarming` sequences - functions to execute each warming cycle
* `LambdaInit` sequences - functions to execute on initialization of lambda
* `HttpRequestInterceptor` - interceptors for HTTP requests, maybe chained
* Possibility to extend serialization and deserialization
* Links built-in support -- base links and links with parameters
