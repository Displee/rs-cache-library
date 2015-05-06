
##Welcome to Displee's cache library!

##About
An application that is written in Java, used to read and write to the caches of RuneScape2.
This application is able to read and write to various types of RuneScape2 caches between the revisions 561 and 743.
A RuneScape2 cache is built of a 2-level container and in each level 1 container is the actual data of the game stored.
I have named the level 0 container "Index" and the level 1 container "Archive". It looks like this.
- Index (level 0)
	- Archive (level 1)
		- File (data)

The containers can contain multiple containers which makes you able to store a great amount of data.
A file contains useful data like the properties of a certain in-game item, object, image or even model.
The application is able to read this data, and write manipulated read data to the cache.

###Features:
- XTEA (en/de)cryption.
- Whirlpool hash generating.
- CRC hash generating.
- BZIP2 compression.
- GZIP compression.
- Reading and writing indices.
- Reading and writing archives.
- Reading and writing files.
- Adding an index.
- Adding and removing archives in an index.
- Adding and removing files in an archive.
- More to come...

######Note: it's only possible to add an index after the last index of a cache. The id of the new index will be the id of the last index increased by 1.

##How to use

###Initialize your cache
```Java
CacheLibrary library = new CacheLibrary("path_to_your_cache", 562);//Cache revision for optimal usage.
```
###Get a specific file
```Java
library.getIndex(19).getArchive(81).getFile(34);//Completionist cape
```
###Add an archive to an index
```Java
library.getIndex(7).addArchive();
```
```Java
library.getIndex(7).addArchive("name");
```
```Java
library.getIndex(7).addArchive(38372);
```
```Java
library.getIndex(7).addArchive(library.getIndex(7).getArchive(3223).copy());
```
```Java
library.getIndex(7).addArchives(library.getIndex(19).getArchives());//Add multiple archives
```
###Add a file to an archive
```Java
library.getIndex(7).addArchive(3223).addFile("file_name", byte_array);
```
```Java
Archive archive = library.getIndex(7).addArchive(3223);
File file = archive.addFile("file_name");
file.setId(52);
archive.addFile(byte_array);
```
```Java
library.getIndex(12).addArchive(1337).addFile("yol0");
```
###Remove an archive or file
```Java
library.getIndex(12).removeArchive(1337);
```
```Java
library.getIndex(12).removeArchive("script_sof");
```
```Java
library.getIndex(12).getArchive(1337).removeFile(0);
```
```Java
library.getIndex(12).getArchive(1337).removeFile("open_bank");
```
###Update your cache after changes (important)
```Java
library.getIndex(7).removeArchive(4746);//Not saved into the cache yet.
library.getIndex(7).getArchive(3223).removeFile(0);//Not saved into the cache yet.
library.getIndex(7).update();//Now its written and saved in the cache.
```
###Cache an index
```Java
library.getIndex(10).cache();
```
```Java
int[][] xteas = new int[5][];
xteas[3] = new int[] { 5, 10, 20 };//Setting xteas for archive 3
library.getIndex(5).cache(xteas);
```
###Data manipulation
```Java
byte[] data = library.getIndex(19).getArchive(81).getFile(34).getData();
data[0] = 5;
library.getIndex(19).getArchive(81).addFile(34, data);
library.getIndex(19).update();
```
###Cross cache copying
```Java
final CacheLibrary from = new CacheLibrary("path_1_here", 742);
final CacheLibrary to = new CacheLibrary("path_2_here", 718);
from.getIndex(3).cache();//Don't forget to cache the index you want to copy from first!
to.getIndex(3).addArchives(from.getIndex(3).getArchives(), true, true);//Copy all interfaces from 742 to your own cache.
to.getIndex(3).update();
```

Easy, isn't it?
There are plenty more functions you can use, check it out!

######Note: if you add or delete archives/files, this is not directly written to the cache, to write it to the cache, you should update your cache.
######Note: if there are any issue's, please report them [here](https://github.com/Displee/RS2-Cache-Library/issues).


###License
Displee's cache library is open-sourced software licensed under the MIT license.
