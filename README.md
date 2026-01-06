<h1> <img width="40" height="40" src="https://s3-eu-west-1.amazonaws.com/public.s3.ktls.aws.intellij.net/resources/favicon.apng" alt="Kotless Icon"> Kotless </h1>

[![JetBrains incubator project](https://jb.gg/badges/incubator.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)
[![KotlinLang slack](https://img.shields.io/static/v1?label=kotlinlang&message=kotless&color=brightgreen&logo=slack&style=flat)](https://app.slack.com/client/T09229ZC6/CKS388069)

Kotless stands for Kotlin serverless framework.

Its focus lies in reducing the routine of serverless deployment creation by generating it straight
from the code of the application itself.

So, simply speaking, Kotless gives you one magic button to deploy your Web application as a
serverless application on AWS!

Kotless consists of two main parts:

* DSL provides a way of defining serverless applications.
    * **Spring Boot** &mdash; Spring Boot serverless container that is introspected by Kotless. Use
      standard Spring syntax and Kotless will generate deployment.
* Kotless Gradle Plugin provides a way of deploying serverless application. For that, it:
    * performs the tasks of generating Terraform code from the application code and, subsequently,
      deploying it to AWS;
    * runs application locally, emulates the AWS environment (if necessary) and provides the
      possibility for IDE debugging.

One of the key features of Kotless is its ability to embed into existing applications. Kotless makes
super easy deployment of existing Spring applications to AWS serverless platform.

## Getting started

### Setting up project

**Important:** Since this is a fork and we cannot release versions to Maven/Gradle repositories, you will need to clone this repository and run the `publishToLocalMaven` task under `kotless/tasks/publishing` to make the Kotless plugin and libraries available locally before using them in your project.

Kotless uses Gradle to wrap around the existing building process and insert the deployment into it.

Consider using one of the latest versions of Gradle, starting with the **8.5** version.

Basically, if you already use Gradle, you only need to do two things.

Firstly, set up the Kotless Gradle plugin.

You will have to tell Gradle where to find the plugin by editing `settings.gradle.kts`:

```kotlin
pluginManagement {
    resolutionStrategy {
        this.eachPlugin {
            if (requested.id.id == "io.kotless") {
                useModule("io.kotless:gradle:${this.requested.version}")
            }
        }
    }

    repositories {
        mavenLocal() // Required for locally published fork version
        maven(url = uri("https://packages.jetbrains.team/maven/p/ktls/maven"))
        gradlePluginPortal()
        mavenCentral()
    }
}
```

And apply the plugin:

```kotlin
//Imports are necessary, for this example
import io.kotless.plugin.gradle.dsl.Webapp.Route53
import io.kotless.plugin.gradle.dsl.kotless

//Group may be used by Kotless DSL to reduce the number of introspected classes by package
//So, don't forget to set it
group = "org.example"
version = "0.1.0"


plugins {
    //Version of Kotlin should be 1.9.21+
    kotlin("jvm") version "1.9.21" apply true

    id("io.kotless") version "0.3.4" apply true
}
```

Secondly, add Spring Boot DSL as a library to your application:

```kotlin
repositories {
    mavenLocal() // Required for locally published fork version
    mavenCentral()
    //Kotless repository
    maven(url = uri("https://packages.jetbrains.team/maven/p/ktls/maven"))
}

dependencies {
    implementation("io.kotless", "spring-boot-lang", "0.3.4")
}
```

This gives you access to DSL interfaces in your code and sets up a Lambda dispatcher inside your
application.

### Deploying to the cloud

Note, that if you even don't have a cloud account, you can still use Kotless locally to run and
debug your application -- just use `local` Gradle task.

#### Deploying to AWS

If you don't have an AWS account, you can create it following simple
[instruction](https://hadihariri.com/2020/05/12/from-zero-to-lamda-with-kotless/) by Hadi Hariri.

Note: the above link by Hadi Hariri is very detailed and thats the reason i kept it but there is a leaner&more updated version [HERE](Aws-Setup.md)

If you have an AWS account and want to perform the real deployment &mdash; let's set up everything
for it! It's rather simple:

```kotlin

kotless {
    config {

        aws {
            storage {
                bucket = "kotless.s3.example.com"
            }

            profile = "example"
            region = "eu-west-1"
        }
    }

    webapp {
        dns("kotless", "example.com")
    }
}
```

Here we set up the config of Kotless itself:

* the bucket, which will be used to store lambdas and configs;
* Terraform configuration with the name of the profile to access AWS.

Then we set up a specific application to deploy:

* Route53 alias for the resulting application (you need to pre-create an ACM certificate for the DNS
  record).

And that's the whole setup!

### Creating application

Now you can create your first serverless application with Spring Boot DSL:
```kotlin
@SpringBootApplication
open class Application : Kotless() {
    override val bootKlass: KClass<*> = this::class
}

@RestController
object Pages {
    @GetMapping("/")
    fun main() = "Hello World!"
}
```

## Local start

Kotless-based applications can start locally as an HTTP server. This functionality is supported by
all DSLs.

Moreover, Kotless local start may spin up an AWS emulation (docker required). Just instantiate your
AWS service client using override for Kotless local starts:

```kotlin
val client = AmazonDynamoDBClientBuilder.standard().withKotlessLocal(AwsResource.DynamoDB).build()
```

And enable it in Gradle:

```kotlin
kotless {
    //<...>
    extensions {
        local {
            //enables AWS emulation (disabled by default)
            useAWSEmulation = true

            //8080 is default if not supplied, this is mainly used when u want to run multiple kotless services local (give different port to each)
            port = 8080
            
            //when supplying debug port the app with allow remote debug via this port (if you want to run multiple kotless locally provide different debug ports to each)
            debugPort = 5005

            //when set to 'true' the local kotless will wait until remote debug is attached 
            suspendDebug = false
        }
    }
}
```

During the local run, LocalStack will be started and all clients will be pointed to its endpoint
automatically.

Local start functionality does not require any access to cloud provider, so you may check how your
application behaves without an AWS account. Also, it gives you the possibility to debug your
application locally from your IDE.

## Integration with existing applications

Kotless is able to deploy existing Spring Boot application to AWS serverless platform. To do
it, you'll need to set up a plugin and replace existing dependency with the appropriate Kotless DSL.

For **Spring Boot** you should replace the starter you use (
e.g. `implementation("org.springframework.boot", "spring-boot-starter-web", "3.2.0)`)
with `implementation("io.kotless", "spring-boot-lang", "0.3.4")`. Note that this dependency bundles
Spring Boot of version `3.2.0`, so you also may need to upgrade other Spring Boot libraries to this
version.

Once it is done, you may hit `deploy` task and make your application serverless. Note, that you will
still be able to run application locally via `local` Gradle task.

## Advanced features

While Kotless can be used as a framework for the rapid creation of serverless applications, it has
many more features covering different areas of application.

Including, but not limited to:

* **Lambdas auto-warming** &mdash; Kotless creates schedulers to execute warming sequences to never
  leave your lambdas cold. As a result, applications under moderate load are not vulnerable to
  cold-start problem.
* **Permissions management** &mdash; you can declare which permissions to which AWS resources are
  required for application via annotations on Kotlin functions, classes or objects. Permissions
  will be granted automatically.
* **Static resources** &mdash; Kotless will deploy static resources to S3 and set up CDN for them.
  It may greatly improve the response time of your application and is supported by all DSLs.
* **Scheduled events** &mdash; Kotless sets up timers to execute `@Scheduled` jobs on schedule;
* **SNS consumers** &mdash; Kotless automatically creates SNS topics, Lambda functions, and subscriptions for functions annotated with `@SNSEvent`. The infrastructure is generated automatically, and you only need to annotate your handler function;
* **Terraform extensions** &mdash; Kotless-generated code can be extended by custom Terraform code;

Kotless is in active development, so we are currently working on extending this list with such
features as:

* Support of multiplatform applications &mdash; Kotless will not use any platform-specific libraries
  to give you a choice of a Lambda runtime (JVM/JS/Native).
* Versioned deployment &mdash; Kotless will be able to deploy several versions of the application
  and maintain one of them as active.
* Implicit permissions granting &mdash; Kotless will be able to deduce permissions from AWS SDK
  function calls.
* Additional event handlers support &mdash; Kotless will generate events subscriptions for other AWS
  event sources (currently SNS is supported).

## Examples

Any explanation becomes much better with a proper example.

you can find examples at https://github.com/mamaorha/kotless-playground/tree/master

### Consuming SNS messages

Kotless makes it easy to consume messages from AWS SNS topics. Simply annotate a function with `@SNSEvent` and Kotless will automatically:

* Create the SNS topic (if it doesn't exist)
* Create a Lambda function for your handler
* Set up a subscription between the topic and Lambda
* Configure the necessary permissions

Here's an example of how to consume SNS messages:

```kotlin
import io.kotless.dsl.cloud.aws.SNSEvent
import io.kotless.dsl.cloud.aws.SNSEventData.SNSRecord

object NotificationHandler {
    @SNSEvent(topicName = "user-notifications")
    fun handleNotification(record: SNSRecord) {
        val message = record.sns.message
        val subject = record.sns.subject
        val topicArn = record.sns.topicArn
        
        println("Received notification from $topicArn")
        println("Subject: $subject")
        println("Message: $message")
        
        // Access message attributes if present
        record.sns.messageAttributes?.forEach { (key, attribute) ->
            println("Attribute $key: ${attribute.value}")
        }
        
        // Your business logic here
    }
    
    // You can also specify a different region for the topic
    @SNSEvent(topicName = "cross-region-notifications", region = "us-east-1")
    fun handleCrossRegionNotification(record: SNSRecord) {
        // Handle messages from a topic in a different region

        val message = record.sns.message
        val subject = record.sns.subject
        val topicArn = record.sns.topicArn

        println("Received notification from $topicArn")
        println("Subject: $subject")
        println("Message: $message")

        // Access message attributes if present
        record.sns.messageAttributes?.forEach { (key, attribute) ->
            println("Attribute $key: ${attribute.value}")
        }

        // Your business logic here
    }
}
```

**Key points:**

* The `@SNSEvent` annotation requires a `topicName` parameter
* The `region` parameter is optional and defaults to your configured AWS region
* Your handler function should accept `SNSEventData` as a parameter
* `SNSRecord` contains record data with an `sns` message
* Each message contains fields like `message`, `subject`, `topicArn`, `timestamp`, and optional `messageAttributes`
* If your handler needs additional AWS permissions (e.g., to write to DynamoDB), use the Permissions API annotations (see [Advanced features](#advanced-features))

### Using GameLift permissions

Kotless allows you to grant AWS GameLift permissions to your Lambda functions using the `@GameLift` annotation. This annotation can be applied to functions, classes, or properties to automatically configure IAM permissions for GameLift resources.

The `@GameLift` annotation always uses `"*"` as the resource since GameLift matchmaking actions (and many other GameLift actions) don't support resource-level permissions.

Here's an example of how to use GameLift permissions:

```kotlin
import io.kotless.dsl.cloud.aws.GameLift
import io.kotless.PermissionLevel

@GameLift(level = PermissionLevel.ReadWrite)
class MatchmakingService {
    fun startMatchmaking() {
        // Your code that calls StartMatchmaking
        // This class now has permissions for:
        // - gamelift:StartMatchmaking
        // - gamelift:StopMatchmaking
        // - gamelift:AcceptMatch
    }
    
    fun stopMatchmaking() {
        // Your code that calls StopMatchmaking
    }
    
    fun acceptMatch() {
        // Your code that calls AcceptMatch
    }
}
```

**Key points:**

* The `@GameLift` annotation only requires a `level` parameter
* The annotation always uses `"*"` as the resource since GameLift matchmaking actions don't support resource-level permissions
* The `level` parameter can be `PermissionLevel.Read`, `PermissionLevel.Write`, or `PermissionLevel.ReadWrite`
* The annotation can be applied to functions, classes, or properties
* Permissions are automatically granted when your application is deployed

## Want to know more?

You may take a look at [Wiki](https://github.com/JetBrains/kotless/wiki) where the client
documentation on Kotless is located.

Apart from that, the Kotless code itself is widely documented, and you can take a look into its
interfaces to get to know Kotless better.

You may ask questions and participate in discussions on `#kotless` channel
in [KotlinLang slack](http://slack.kotlinlang.org).

## Acknowledgements

Special thanks to:

* Alexandra Pavlova (aka sunalex) for our beautiful logo;
* Yaroslav Golubev for help with documentation;
* [Gregor Billing](https://github.com/suushiemaniac) for help with the Gradle plugin and more.
