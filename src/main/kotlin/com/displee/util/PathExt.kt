package com.displee.util

import java.nio.file.Files
import java.nio.file.Path

fun Path.createFileIfNotExists(): Boolean {
    if (Files.exists(this)) return true

    try {
        Files.createFile(this)
        return true
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}