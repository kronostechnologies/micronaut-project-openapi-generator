# Micronaut client OpenAPI generator

An OpenAPI generator that outputs Micronaut `@Client` interfaces and gradle project files. The output will be ready to build and deploy so that it can be reused across many other projects.

Micronaut interfaces are produced by [kokuwaio/micronaut-openapi-codegen](https://github.com/kokuwaio/micronaut-openapi-codegen), with some minor adjustments to comply with the gradle project structure.

## Usage

This package must be used in conjunction with the OpenAPI generator plugin and is accessible on [GitHub Package Repository](https://github.com/kronostechnologies/micronaut-project-openapi-generator/packages/914811). Do note that at this time GPR only allows authenticated access.

### Example usage

**buildSrc/build.gradle.kts**
```kotlin
plugins {
    id("net.linguica.maven-settings") version "0.5"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.github.com/kronostechnologies/*/") {
        name = "github"
        credentials {
            username = project.findProperty("gpr.user")?.toString()
                ?: System.getenv("GPR_USER")
            password = project.findProperty("gpr.key")?.toString()
                ?: System.getenv("GPR_TOKEN")
        }
    }
}

dependencies {
    implementation("com.equisoft.openapi.generator.micronaut:micronaut-project-openapi-generator:1.0.0")
}
```

**app/build.gradle.kts**
```kotlin
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.openapi.generator") version "5.2.0"
    application
}

tasks {
    register<GenerateTask>("generateMicronautSdk") {
        group = "openapi tools"

        dependsOn("kaptKotlin")

        generatorName.set("micronaut")

        val generatedSwaggerSpec = "$buildDir/tmp/kapt3/classes/main/META-INF/swagger/__your-api-name__-$version.yml"
        inputSpec.set(generatedSwaggerSpec)
        outputDir.set("$buildDir/sdk/micronaut")

        id.set("${rootProject.name}-sdk")
        groupId.set("${project.group}.sdk")
        version.set("${project.version}")
        
        val sdkPackage = "__your-sdk-package__"
        packageName.set(sdkPackage)
        apiPackage.set(sdkPackage)
        invokerPackage.set("$sdkPackage.invoker")
        modelPackage.set("$sdkPackage.models")

        configOptions.set(
            mapOf(
                "clientId" to "account-service",
                "introspected" to "true",
                "jacksonDatabindNullable" to "false",
                "supportAsync" to "false",
                "useGenericResponse" to "true",
                "useOptional" to "false",
                "useReferencedSchemaAsDefault" to "false"
            )
        )
    }
}
```

**Makefile**
```makefile
BASE_DIR := $(dir $(realpath $(firstword $(MAKEFILE_LIST))))
SDK_MICRONAUT_DIR := $(addsuffix build/sdk/micronaut, $(BASE_DIR))

GRADLEW = ../gradlew
MKDIR_P = mkdir -p

exit_error = (>&2 echo -e ">> \x1B[31m$1\x1B[39m" && exit 1)

.PHONY: package.sdk.micronaut
package.sdk.micronaut: $(SDK_MICRONAUT_DIR)
	@echo "Cleaning up SDK directory"
	@cd ${SDK_MICRONAUT_DIR} && [ "$$(git rev-parse --show-toplevel)" == "${SDK_MICRONAUT_DIR}" ] || $(call exit_error,${SDK_MICRONAUT_DIR} is not a git repository. Run "make clean" and repeat.)
	@cd ${SDK_MICRONAUT_DIR} && git stash save --keep-index --include-untracked && git checkout main && git fetch && git reset --hard origin/main
	@rm -rf ${SDK_MICRONAUT_DIR}/src ${SDK_MICRONAUT_DIR}/docs
	@echo "Generating Micronaut SDK"
	@${GRADLEW} generateMicronautSdk
	@echo "Test Micronaut SDK"
	@cd ${SDK_MICRONAUT_DIR} && ${GRADLEW} build
	@echo "Kotlin SDK generated to: '${SDK_MICRONAUT_DIR}'"

$(SDK_MICRONAUT_DIR): $(SDK_DIR)
	@echo "Cloning Micronaut SDK"
	@git clone git@github.com:kronostechnologies/account-service-sdk-micronaut.git $(SDK_MICRONAUT_DIR)

$(SDK_DIR):
	@${MKDIR_P} ${SDK_DIR}
```

Note that when GitHub allows unauthenticated access to GPR, the `buildSrc` config can be removed and this can be added to `app/build.gradle.kts`:
```kotlin
buildscript {
    repositories {
        maven("https://maven.pkg.github.com/kronostechnologies/*/")
    }

    dependencies {
        implementation("com.equisoft.openapi.generator.micronaut:micronaut-project-openapi-generator:1.0.0")
    }
}
```

## Publish

### Automatically

GitHub Actions will build and publish tags that conform to the format `vx.x.x`. Note that only commits based on master can be published.

Once ready to publish, simply tag the desired commit and push it.

### Manually

This project uses the `maven-settings` plugin fill in the authentication to GPR. Make sure you have a PAT with write access to packages configurer in your `~/.m2/settings.xml`:
```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>__GITHUB-USERNAME__</username>
      <password>__GITHUB-PAT-READ-ONLY__</password>
    </server>
    <server>
      <id>gprWrite</id>
      <username>__GITHUB-USERNAME__</username>
      <password>__GITHUB-PAT-READ-WRITE__</password>
    </server>
  </servers>
</settings>
```

From there, you can run `./gradlew clean assemble publish -Papplication.version=0.1.2`.
