rootProject.name = "micronaut-project-openapi-generator"

include("lib")

val isCiServer = System.getenv().containsKey("CI")

buildCache {
    local {
        isEnabled = !isCiServer
    }
}
