rootProject.name = "kotless"

include(":lib")
include(":schema")
include(":model")
include(":engine")

include(":dsl:common:dsl-common")
include(":dsl:common:cloud:dsl-common-aws")
include(":dsl:common:dsl-parser-common")

include(":dsl:spring:spring-boot-lang")
include(":dsl:spring:spring-boot-lang-local")
include(":dsl:spring:spring-lang-parser")

include(":runtimes:graal-runtime")
include(":plugins:gradle")

