rootProject.name = "rs-cache-library"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.22"
        id("com.github.johnrengelman.shadow") version "8.1.1"
        id("io.github.gradle-nexus.publish-plugin") version "2.0.0-rc-2"
    }
}