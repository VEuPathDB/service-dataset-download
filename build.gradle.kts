import org.veupathdb.lib.gradle.container.util.Logger.Level

plugins {
  java
  id("org.veupathdb.lib.gradle.container.container-utils") version "4.6.0"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

// configure VEupathDB container plugin
containerBuild {

  // Change if debugging the build process is necessary.
  logLevel = Level.Info

  // General project level configuration.
  project {

    // Project Name
    name = "dataset-download-service"

    // Project Group
    group = "org.veupathdb.service"

    // Project Version
    version = "3.0.0"

    // Project Root Package
    projectPackage = "org.veupathdb.service"

    // Main Class Name
    mainClassName = "dsdl.Main"
  }

  // Docker build configuration.
  docker {

    // Docker build context
    context = "."

    // Name of the target docker file
    dockerFile = "Dockerfile"

    // Resulting image tag
    imageName = "dataset-download"

  }

  generateJaxRS {
    // List of custom arguments to use in the jax-rs code generation command
    // execution.
    arguments = listOf(/*arg1, arg2, arg3*/)

    // Map of custom environment variables to set for the jax-rs code generation
    // command execution.
    environment = mapOf(/*Pair("env-key", "env-val"), Pair("env-key", "env-val")*/)
  }

}

tasks.register("print-gen-package") { print("org.veupathdb.service") }

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

tasks.shadowJar {
  exclude("**/Log4j2Plugins.dat")
  archiveFileName.set("service.jar")
}

repositories {
  mavenCentral()
  mavenLocal()
  maven {
    name = "GitHubPackages"
    url  = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
    credentials {
      username = if (extra.has("gpr.user")) extra["gpr.user"] as String? else System.getenv("GITHUB_USERNAME")
      password = if (extra.has("gpr.key")) extra["gpr.key"] as String? else System.getenv("GITHUB_TOKEN")
    }
  }
}

//
// Project Dependencies
//

// versions
val coreLib       = "6.13.1"        // Container core lib version
val edaCommon     = "10.1.0"        // EDA Common version
val fgputil       = "2.9.3-jakarta" // FgpUtil version

// use local EdaCommon compiled schema if project exists, else use released version;
//    this mirrors the way we use local EdaCommon code if available
val edaCommonLocalProjectDir = findProject(":edaCommon")?.projectDir
val edaCommonSchemaFetch =
  if (edaCommonLocalProjectDir != null)
    "cat ${edaCommonLocalProjectDir}/schema/library.raml"
  else
    "curl https://raw.githubusercontent.com/VEuPathDB/EdaCommon/v${edaCommon}/schema/library.raml"

// register a task that prints the command to fetch EdaCommon schema; used to pull down raml lib
tasks.register("print-eda-common-schema-fetch") { print(edaCommonSchemaFetch) }

// ensures changing modules are never cached
configurations.all {
  resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

dependencies {

  // VEuPathDB libs, prefer local checkouts if available
  implementation(findProject(":core") ?: "org.veupathdb.lib:jaxrs-container-core:${coreLib}")
  implementation(findProject(":edaCommon") ?: "org.veupathdb.service.eda:eda-common:${edaCommon}")

  // published VEuPathDB libs
  implementation("org.gusdb:fgputil-core:${fgputil}")
  implementation("org.gusdb:fgputil-db:${fgputil}")
  implementation("org.gusdb:fgputil-client:${fgputil}")

  // Jersey
  implementation("org.glassfish.jersey.core:jersey-server:3.1.0")

  // Jackson
  implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
  implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.0")

  // Log4J
  implementation("org.apache.logging.log4j:log4j-api:2.19.0")
  implementation("org.apache.logging.log4j:log4j-core:2.19.0")

  // Metrics
  implementation("io.prometheus:simpleclient:0.16.0")
  implementation("io.prometheus:simpleclient_common:0.16.0")

  // Utils
  implementation("io.vulpine.lib:Jackfish:1.1.0")
  implementation("com.devskiller.friendly-id:friendly-id:1.1.0")

  // Unit Testing
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
  testImplementation("org.mockito:mockito-core:4.8.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

val test by tasks.getting(Test::class) {
  // Use junit platform for unit tests
  useJUnitPlatform()
}
