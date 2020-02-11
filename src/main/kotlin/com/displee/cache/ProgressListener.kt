package com.displee.cache

interface ProgressListener {
    fun notify(progress: Double, message: String?)
    fun finish(title: String?, message: String?)
}