plugins {
    kotlin("jvm") version "1.9.21"
    `maven-publish`
    signing
}

group = "com.displee"
version = "7.0"

description = "A library written in Kotlin used to read and write to all cache formats of RuneScape."

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.jponge:lzma-java:1.3")
    implementation("org.apache.ant:ant:1.10.11")
    implementation("com.displee:disio:2.2")
    testImplementation("junit:junit:4.13.1")
}

tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn.add(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
        archives(jar)
    }
}

val ossrhUsername: String by project
val ossrhPassword: String by project

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

kotlin {
    jvmToolchain(8)
}