import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val javaVersion = JavaVersion.VERSION_13

group = "com.equisoft.openapi.generator.micronaut"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("net.linguica.maven-settings") version "0.5"
    id("com.equisoft.standards.kotlin") version "0.5.0"

    jacoco
    `java-library`
    `maven-publish`
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.openapitools:openapi-generator:5.2.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.1")
    implementation("io.kokuwa.micronaut:micronaut-openapi-codegen:2.1.8")
    implementation("io.micronaut:micronaut-core:2.5.11")
}

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

jacoco {
    toolVersion = "0.8.6"
}

publishing {
    repositories {
        maven {
            name = "micronaut-project-openapi-generator"
            url = uri("https://maven.pkg.github.com/kronostechnologies/micronaut-project-openapi-generator")
            credentials {
                name = "gprWrite"
                username = project.findProperty("gpr.user")?.toString() ?: System.getenv("GHCR_USER")
                password = project.findProperty("gpr.key")?.toString() ?: System.getenv("GHCR_TOKEN")
            }
        }
    }

    publications {
        create<MavenPublication>("gpr") {
            artifactId = rootProject.name

            from(components["java"])

            pom {
                name.set("Micronaut Project OpenAPI Generator")
                description.set("OpenAPI project generator for Micronaut clients and SDKs")
                url.set("https://github.com/kronostechnologies/micronaut-project-openapi-generator")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/kronostechnologies/micronaut-project-openapi-generator/")
                }
            }
        }
    }
}

tasks {
    check {
        dependsOn(jacocoTestCoverageVerification)
    }

    test {
        finalizedBy("jacocoTestReport")
        useJUnitPlatform()
    }

    jacocoTestReport {
        reports {
            csv.required.set(false)
            xml.required.set(true)
            html.required.set(true)
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.majorVersion
            javaParameters = true
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xopt-in=kotlin.contracts.ExperimentalContracts"
            )
        }
    }

    register("ci-classes") {
        group = "ci"
        dependsOn(testClasses)
    }

    register("ci-check") {
        group = "ci"

        dependsOn("ci-classes")

        dependsOn(detekt)
        dependsOn(lintKotlin)
    }

    register("ci-unit-tests") {
        group = "ci"

        dependsOn("ci-classes")

        dependsOn(test)
        dependsOn(jacocoTestCoverageVerification)
    }
}
