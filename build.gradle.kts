import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.20"
    `java-gradle-plugin`
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
    id("org.camuthig.credentials") version "0.1.0"
}

group = "org.camuthig.credentials"
version = "0.1.0"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/camuthig/maven")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.camuthig.credentials:core:0.1.1")
    testImplementation(kotlin("test-junit"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("test/resources")

gradlePlugin {
    plugins {
        register("credentials") {
            id = "org.camuthig.credentials"
            implementationClass = "org.camuthig.credentials.gradle.CredentialsPlugin"
        }
    }
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    repositories {
        maven {
            url = uri("$buildDir/repo")
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = "org.camuthig.credentials.gradle.plugin"

            pom {
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("camuthig")
                        name.set("Chris Muthig")
                    }
                }

                scm {
                    url.set("https://github.com/camuthig/kotlin-credentials")
                }
            }
        }
    }
}

bintray {
    user = System.getProperty("bintray.user")
    key = System.getProperty("bintray.key")
    publish = true
    setPublications("maven")
    pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "credentials-gradle"
        websiteUrl = "https://camuthig.dev"
        githubRepo = "camuthig/credentials-gradle"
        vcsUrl = "https://github.com/camuthig/credentials-gradle"
        description = "A Gradle plugin for maintaining an encrypted credentials configuration file."
        setLabels("kotlin")
        setLicenses("Apache-2.0")
        desc = description
    })
}