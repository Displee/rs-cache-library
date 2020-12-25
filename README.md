
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
- Easy to use
- Very fast (including OSRS)
- CRUD operations for indices, archives and files
- Cross cache copying
- XTEA cryption
- BZIP2, GZIP and LZMA (de)compression
- Whirlpool and CRC generation
- Ukeys generation
- Cache rebuilding (also known as cache defragmentation)

## Gradle
```
implementation 'com.displee:rs-cache-library:6.8'
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
val xtea = intArrayOf(0, 0, 0, 0) //optional
val landscape = library.data(5, "l${x}_${y}", xtea)
```
For our 317 users :)
```kotlin
val objData = library.data(0, 2, "obj.dat")
val objMeta = library.data(0, 2, "obj.idx")
```
#### Put file data
```kotlin
val xtea = intArrayOf(...) //optional
library.put(18, 10, 2, byteArrayOf(...), xtea)
```
```kotlin
val xtea = intArrayOf(...) //optional
library.put(5, "l60_62", byteArrayOf(...), xtea)
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
library.index(7).update() //returns true if changes have been written with success, else false
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
val file = library.idnex(5).archive("l${x}_${y}", xtea)?.add(0, byteArrayOf(...), replace)
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
library.index(7).cache()
```
If you want to cache an index containing xteas, set the xteas first for the archives:
```kotlin
for(regionId in 0 until 255 * 255) {
    val x = (regionId shr 8) and 0xFF
    val y = regionId and 0xFF
    val xtea = RegionManager.getXTEA(regionId)
    library.index(5).archive("l${x}_${y}", true)?.xtea(xtea)
}
library.index(5).cache()
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
#### Cache rebuilding
When you remove an archive from an index, only the reference is being deleted.
The actual data is still accessible (hence why the function is called 'remove' and not 'delete').
So why is the data not being removed? This has basically to do with how binary files are built.

If you want to delete something in the middle of a binary file, all data after it has to be shifted to the left.
With other words, whole cache has to be rebuilt in order to delete archive data.

To do this, you can use the following function:
```kotlin
//I only recommend this if you deleted a lot of archives and really want to shrink your cache
library.rebuild(File("location/of/new/cache"))
```
---
#### Example (replace musics in a cache with the ones from another cache)
```kotlin
val cacheFrom = CacheLibrary.create("...")
val cacheTo = CacheLibrary.create("...")
val index = cacheTo.index(6)
cacheFrom.index(6).cache()
index.clear()
index.add(*cacheFrom.index(6).archives())
index.update()
```
---
Easy, isn't it?
There are plenty more functions you can use, check it out!

###### Note: if there are any issue's, please report them [here](https://github.com/Displee/RS2-Cache-Library/issues).


### License
Displee's cache library is open-sourced software licensed under the MIT license.
