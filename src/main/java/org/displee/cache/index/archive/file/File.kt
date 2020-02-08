package org.displee.cache.index.archive.file

class File(val id: Int, var data: ByteArray? = null, var hashName: Int = -1) : Comparable<File> {

    constructor(id: Int) : this(id, null, -1)

    constructor(id: Int, data: ByteArray?) : this(id, data, -1)

    constructor(file: File): this(file.id, file.data?.clone(), file.hashName)

    override fun compareTo(other: File): Int {
        return id.compareTo(other.id)
    }

}