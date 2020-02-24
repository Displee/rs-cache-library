package com.displee.cache.index

import com.displee.cache.CacheLibrary
import com.displee.cache.ProgressListener
import java.io.RandomAccessFile

class Index255(origin: CacheLibrary, raf: RandomAccessFile) : Index(origin, 255, raf) {

    override fun update(listener: ProgressListener?): Boolean {
        throw IllegalAccessException("Not allowed.")
    }

}