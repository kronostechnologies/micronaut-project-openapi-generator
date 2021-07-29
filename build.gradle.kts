import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
}

allprojects {
    version = project.findProperty("application.version")?.toString() ?: "0.0.0-SNAPSHOT"

    apply(plugin = "com.github.ben-manes.versions")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://maven.pkg.github.com/kronostechnologies/*/") {
            name = "github"
            credentials {
                username = project.findProperty("gpr.user")?.toString()
                    ?: System.getenv("GPR_USER")
                    ?: System.getenv("GHCR_USER")
                password = project.findProperty("gpr.key")?.toString()
                    ?: System.getenv("GPR_TOKEN")
                    ?: System.getenv("GHCR_TOKEN")
            }
        }
    }

    tasks {
        named<DependencyUpdatesTask>("dependencyUpdates").configure {
            fun isStable(version: String): Boolean {
                val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
                val regex = "^[0-9,.v-]+(-r)?$".toRegex()
                return stableKeyword || regex.matches(version)
            }

            checkConstraints = true
            gradleReleaseChannel = "current"
            outputFormatter = "json,html"

            rejectVersionIf {
                !isStable(candidate.version) && isStable(currentVersion)
            }
        }
    }
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        distributionSha256Sum = "9bb8bc05f562f2d42bdf1ba8db62f6b6fa1c3bf6c392228802cc7cb0578fe7e0"
        gradleVersion = "7.1.1"
    }
}
