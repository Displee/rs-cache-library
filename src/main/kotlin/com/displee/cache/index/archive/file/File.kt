package com.displee.cache.index.archive.file

class File : Comparable<File> {

    val id: Int
    var data: ByteArray?
    var hashName: Int

    constructor(id: Int, data: ByteArray? = null, hashName: Int = 0, @Suppress("UNUSED_PARAMETER") checkData: Boolean = false) {
        this.id = id
        this.data = data
        this.hashName = hashName
    }

    @JvmOverloads
    constructor(id: Int, data: ByteArray, hashName: Int = 0) : this(id, data, hashName, true)

    constructor(file: File) : this(file.id, file.data?.clone(), file.hashName)

    override fun compareTo(other: File): Int {
        return id.compareTo(other.id)
    }

    override fun toString(): String {
        return "File $id[data_length=${data?.size}, hash_name=$hashName]"
    }

}