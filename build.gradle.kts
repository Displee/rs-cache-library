plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")

    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

group = "com.displee"
version = "7.2.0"

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

nexusPublishing {
    repositories {
        sonatype {
            if (false) { // only for users registered in Sonatype after 24 Feb 2021
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            }

            if (ossrhUsername != null && ossrhPassword != null) {
                username.set(ossrhUsername)
                password.set(ossrhPassword)
            }
        }
    }
}