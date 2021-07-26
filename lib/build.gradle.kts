group = "com.equisoft.openapi.generator.micronaut"
version = project.findProperty("application.version") ?: "0.0.0-SNAPSHOT"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("net.linguica.maven-settings") version "0.5"

    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.openapitools:openapi-generator:5.2.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.1")
    implementation("io.kokuwa.micronaut:micronaut-openapi-codegen:2.1.8")
    implementation("io.micronaut:micronaut-core:2.5.11")
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
