plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.21"

    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    api("org.openapitools:jackson-databind-nullable:0.2.1")

    implementation("org.openapitools:openapi-generator:5.2.0")
    implementation("io.kokuwa.micronaut:micronaut-openapi-codegen:2.1.8")
}
