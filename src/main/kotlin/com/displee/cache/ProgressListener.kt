package com.displee.cache

@FunctionalInterface
interface ProgressListener {

    fun notify(progress: Double, message: String?)

}