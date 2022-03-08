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
        distributionSha256Sum = "cd5c2958a107ee7f0722004a12d0f8559b4564c34daad7df06cffd4d12a426d0"
        gradleVersion = "7.4"
    }
}
