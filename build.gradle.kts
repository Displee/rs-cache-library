import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Base64

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")

    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

group = "com.displee"
version = "8.0.1"

description = "A library written in Kotlin used to read and write to all cache formats of RuneScape."

kotlin {
    jvmToolchain(8)
}

dependencies {
    implementation("com.github.jponge:lzma-java:1.3")
    implementation("org.apache.ant:ant:1.10.14")
    implementation("com.displee:disio:2.3")
    testImplementation(kotlin("test"))
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
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}

tasks.register("uploadToCentralPortal") {
    group = "publishing"
    description = "Uploads the current Sonatype staging repository to the Maven Central Portal"

    doLast {
        if (ossrhUsername.isNullOrBlank() || ossrhPassword.isNullOrBlank()) {
            throw GradleException("Missing centralUsername/centralPassword. Define them in gradle.properties or environment variables.")
        }

        val namespace = "com.displee"
        val bearer = Base64.getEncoder().encodeToString("$ossrhUsername:$ossrhPassword".toByteArray())

        println("🔍 Searching for open staging repositories for $namespace ...")
        val searchUrl = "https://ossrh-staging-api.central.sonatype.com/manual/search/repositories?ip=any&profile_id=$namespace"
        val searchCmd = listOf("curl", "-s", "-H", "Authorization: Bearer $bearer", searchUrl)
        val searchOutput = ProcessBuilder(searchCmd)
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .readText()
        println(searchOutput)

        val keyRegex = Regex(
            """\{\s*"key"\s*:\s*"([^"]+)"\s*,\s*"state"\s*:\s*"([^"]+)"(?:\s*,\s*"description"\s*:\s*"([^"]*)")?"""
        )
        val repoMatches = keyRegex.findAll(searchOutput).toList()
        if (repoMatches.isEmpty()) {
            throw GradleException("❌ No repositories found in response:\n$searchOutput")
        }
        val openRepos = repoMatches.filter { it.groupValues[2] == "open" }
        if (openRepos.isEmpty()) {
            throw GradleException("❌ No open repositories found (all are closed or released).")
        }
        val descriptionRegex = Regex("""\bcom\.displee:rs-cache-library:$version\b""")
        val targetRepo = openRepos.find { descriptionRegex.containsMatchIn(it.groupValues[3]) }
            ?: openRepos.first()
        val repoKey = targetRepo.groupValues[1]

        println("✅ Found staging repository key: $repoKey")

        val encodedKey = URLEncoder.encode(repoKey, StandardCharsets.UTF_8.toString())
        val uploadUrl =
            "https://ossrh-staging-api.central.sonatype.com/manual/upload/repository/$encodedKey?publishing_type=automatic"

        println("🚀 Uploading repository to Central Portal...")
        val uploadCmd = listOf(
            "curl", "-i", "-X", "POST",
            "-H", "Authorization: Bearer $bearer",
            uploadUrl
        )
        val uploadOutput = ProcessBuilder(uploadCmd)
            .redirectErrorStream(true)
            .start()
            .inputStream
            .bufferedReader()
            .readText()

        println("📦 Upload response:\n$uploadOutput")
        println("✅ Done! Check https://central.sonatype.com/publishing/deployments in a few minutes.")
    }
}