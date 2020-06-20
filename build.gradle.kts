
// gradle tasks
// gradle check
// gradle publishMavenPublicationToMavenRepository

allprojects {
    val artifactory_dependencies_url: String by project
    repositories {
        // google()
        // jcenter()
        maven {
            url = uri(artifactory_dependencies_url)
        }
    }

    group = "com.github.gmazzo"
    version = "1.3.2"
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}

