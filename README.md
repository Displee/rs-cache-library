
## Displee's cache library
An library written in Kotlin which is used to read and write to all cache types of RuneScape.
A RuneScape cache is built off from a 2-level container structure and in each level 1 container is the actual data of the game stored.
I have named the level 0 container "Index" and the level 1 container "Archive". It looks like this.
- Index (level 0 container)
	- Archive (level 1 container)
		- File (data)

The containers can contain multiple containers which makes you able to group and store a great amount of data.
A file contains useful data like the properties of a certain game item, object, image or even 3D model.
This library is able to read this data, and write manipulated data back to the cache.

### Features:
- easy to use
- very fast
- CRUD operations for indices, archives and files
- cross cache copying
- XTEA cryption
- BZIP2, GZIP and LZMA (de)compression
- whirlpool and CRC generation
- ukeys generation

## Gradle
```
implementation 'com.displee:rs-cache-library:6.0'
```
## Initialize your cache
```kotlin
val library = CacheLibrary("path_to_your_cache")
```
```kotlin
val library = CacheLibrary.create("path_to_your_cache")
```
## Simple usage
#### Get file data
```kotlin
val ags = 11694 //armadyl godsword
val agsData = library.data(19, ags shr 8, ags and 0xFF)
```
```kotlin
val regionId = 12850
val x = (regionId shr 8) and 0xFF
val y = regionId and 0xFF
val landscape = library.data(5, "l$x'_'$y")
```
```kotlin
val objData = library.data(0, 2, "obj.dat")
val objMeta = library.data(0, 2, "obj.idx")
```
#### Put file data
```kotlin
library.put(18, 10, 2, byteArrayOf(...))
```
```kotlin
library.put(5, "l60_62", byteArrayOf(...))
```
#### Remove archive/file
```kotlin
library.remove(5, "l60_62")
```
```kotlin
library.remove(18, 10, 2)
```
#### Write your changes (important)
Update a specific index
```kotlin
val xteas = mapOf(0 to intArrayOf(...)) //optional
library.index(7).update(xteas) //returns true if changes have been written with success, else false
```
or just update all changed indices with one line
```kotlin
val xteas = mapOf("l50_50" to intArrayOf(...)) //required when updating maps index (5), otherwise optional
library.update(xteas)
```
## Advanced usage
#### Add an archive to an index
```kotlin
val newInterface = library.index(3).add()
```
```kotlin
val modelData = byteArrayOf(...)
val newModel = library.index(7).add(modelData)
val newModelId = newModel.id
```
```kotlin
val newArchive = library.index(7).add("custom_name")
```
```kotlin
//returns existing archive if it already existed
val newArchive = library.index(7).add(38372)
```
```kotlin
//add archive with a new id
val archiveToCopy = library.index(3).archive(1200)
if (archiveToCopy == null) {
    //Do something...
    return
}
val newId = 1500 //optional
val replace = true //optional, replace whole archive
val newArchive = library.index(7).add(archiveToCopy, newId, replace)
```
```kotlin
//add multiple archives if they don't exist
val replace = true //optional, replace all archives
library.index(3).add(*otherLibrary.index(3).archives(), replace)
```
#### Add a file to an archive
```kotlin
val newFile = library.index(19).archive(2).add(byteArrayOf(...))
```
```kotlin
val xtea = intArrayOf(...)
val regionId = 12341 //barbarian village
val x = (regionId shr 8) and 0xFF
val y = regionId and 0xFF
val replace = true //optional
val file = library.idnex(5).archive("l$x'_'$y", xtea)?.add(0, byteArrayOf(...), replace)
```
```kotlin
val replace = true //optional
val file = library.index(0).archive(2)?.add("obj.dat", byteArrayOf(...), replace)
```
#### Remove an archive or file
```kotlin
val archive = library.index(7).remove(10)
```
```kotlin
val archive = library.index(5).remove("l50_50")
```
```kotlin
val file = library.index(7).archive(10)?.remove(10)
```
#### Cache an index
Sometimes it's handy to read all data of an entire index and prepare this data for certain operations.
```kotlin
val xteaMap = mutableMapOf<Int, IntArray>()
xteaMap[28392] = intArrayOf(2, 3, 4, 5) //set xteas for archive 28392
library.index(7).cache(xteaMap) //xteaMap is optional
```
```kotlin
val xteaMap = mutableMapOf<String, IntArray>()
for(regionId in 0 until 255 * 255) {
    xteaMap[i] = RegionManager.getXTEA(regionId)
}
library.index(5).cacheByName(xteaMap) //xteaMap is optional
```
#### Cross cache copying
This is actually done in the above examples. You can copy archives and files from one cache to another.

#### Generate ukeys
```kotlin
val exponent = BigInteger(...)
val modulus = BigInteger(...)
val newUkeys = library.generateNewUkeys(exponent, modulus)
```
Generate old ukeys for < 600 caches
```kotlin
val oldUkeys = library.generateOldUkeys()
```
---
#### Example (replace musics in a cache with the ones from another cache)
```kotlin
val cacheFrom = CacheLibrary.create("...")
val cacheTo = CacheLibrary.create("...")
val index = cacheTo.index(6)
index.add(*cacheFrom.index(6).archives())
index.update()
```
---
Easy, isn't it?
There are plenty more functions you can use, check it out!

###### Note: if there are any issue's, please report them [here](https://github.com/Displee/RS2-Cache-Library/issues).


### License
Displee's cache library is open-sourced software licensed under the MIT license.
