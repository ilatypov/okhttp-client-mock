pluginManagement {
    val artifactory_plugin_dependencies_url: String by settings
    repositories {
        maven {
            url = uri(artifactory_plugin_dependencies_url)
        }
    }
}

include (":library")

// Avoid building the top-level directory as a module
rootProject.name = "okhttp-mock-client"
