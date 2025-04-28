plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")

    `maven-publish`
    signing
}

group = "com.displee"
version = "7.3.0"

description = "A library written in Kotlin used to read and write to all cache formats of RuneScape."

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation("com.github.jponge:lzma-java:1.3")
    implementation("org.apache.ant:ant:1.10.14")
    implementation("com.displee:disio:2.2")
}

java {
    withJavadocJar()
    withSourcesJar()
}

val ossrhUsername: String? by project
val ossrhPassword: String? by project

publishing {
    repositories {
        maven {
            val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name = rootProject.name
                description = rootProject.description
                url = "https://github.com/Displee/rs-cache-library"
                packaging = "jar"
                licenses {
                    license {
                        name = "MIT License"
                        url = "https://github.com/Displee/rs-cache-library/blob/master/LICENSE"
                    }
                }
                developers {
                    developer {
                        id = "Displee"
                        name = "Yassin Amhagi"
                        email = "displee@hotmail.com"
                    }
                    developer {
                        id = "Greg"
                        name = "Greg"
                        email = "greg@gregs.world"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/Displee/rs-cache-library.git"
                    developerConnection = "scm:git:ssh://git@github.com/Displee/rs-cache-library.git"
                    url = "https://github.com/Displee/rs-cache-library"
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}