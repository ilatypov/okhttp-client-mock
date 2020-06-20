
// https://docs.gradle.org/6.5/userguide/publishing_maven.html
// https://docs.gradle.org/6.5/userguide/publishing_customization.html

// gradle tasks
// [..]
// publishMavenPublicationToMavenRepository
// [..]

plugins {
    `java-library`
    // `jacoco`
    `maven-publish`
    // id("com.jfrog.artifactory") version "4.15.2"
    // id("java-gradle-plugin")
    // id("org.gradle.kotlin.kotlin-dsl")
    // id("org.jetbrains.kotlin.kotlin-stdlib") version "1.3.72"
    // kotlin("jvm") version "1.3.72"
    // id("io.spring.dependency-management")
    // `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

dependencies {
    compileOnly("com.squareup.okhttp3:okhttp:3.8.1")
    compileOnly("org.robolectric:robolectric:3.4.2")
    compileOnly("com.android.support:support-annotations:25.3.1")
    compileOnly("com.google.android:android:2.2.1")

    // implementation("org.jetbrains.kotlin:kotlin-stdlib:1.3.72")
    // implementation(kotlin("stdlib"))

    testImplementation(configurations.compileOnly)
    testImplementation("junit:junit:4.12")
    // implementation("org.jfrog.buildinfo:build-info-extractor-gradle:latest.release")
}

tasks.withType(JacocoReport::class.java) {
    reports {
        xml.isEnabled = true
        html.isEnabled = true
    }

    tasks["check"].dependsOn(this)
}

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
    classifier = "javadoc"
    from(sourceSets["main"].allSource)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${group}"
            artifactId = "${rootProject.name}"
            version = "${version}"
            val deps = mutableMapOf<String, MutableSet<String>>()
            project.configurations.forEach { conf ->
                println("Configuration: ${conf.name}")
                val mavenScope = (if (conf.name.startsWith("compile")) "compile" else
                        if (conf.name.startsWith("runtime")) "runtime" else
                        if (conf.name.startsWith("test")) "test" else "")
                if (mavenScope != "") {
                    if (!(mavenScope in deps)) {
                        deps[mavenScope] = mutableSetOf<String>()
                    }
                    conf.allDependencies.forEach { dep ->
                        if (dep.group != null) {
                            val depRepr = "${dep.group}:${dep.name}:${dep.version}"
                            println("      ${depRepr}")
                            deps[mavenScope]?.let { it += depRepr }
                        }
                    }
                }
            }
            from(components["java"])
            // artifact(sourcesJar)
            // artifact(javadocJar)
            // artifact("$buildDir/libs/${artifactId}-${version}.jar")

            /*
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            */
            pom {
                withXml {
                    asNode().appendNode("dependencies").let {
                        for (scope in listOf("compile", "runtime", "test")) {
                            for (depRepr in deps.getOrDefault(scope, mutableSetOf<String>()).sorted()) {
                                val (dgroup, dname, dversion) = depRepr.split(":")
                                it.appendNode("dependency").apply {
                                    appendNode("groupId", dgroup)
                                    appendNode("artifactId", dname)
                                    appendNode("version", dversion)
                                    appendNode("scope", scope)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    val artifactory_user: String by project
    val artifactory_password: String by project
    val artifactory_publish_url: String by project
    repositories {
        maven {
            url = uri(artifactory_publish_url)
            credentials {
                username = artifactory_user
                password = artifactory_password
            }
        }
    }
}

