package com.displee.cache.index

import com.displee.cache.CacheLibrary
import java.io.RandomAccessFile

class ChecksumTable(origin: CacheLibrary, id: Int, randomAccessFile: RandomAccessFile) : Index(origin, id, randomAccessFile) {

    fun generate(): ByteArray {
        val data = byteArrayOf()
        //TODO Finish this
        return data
    }

}