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
    }
}