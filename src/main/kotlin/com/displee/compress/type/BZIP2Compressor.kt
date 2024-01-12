package com.displee.compress.type

import com.displee.io.impl.InputBuffer
import org.apache.tools.bzip2.CBZip2OutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * A class representing the BZIP2 (de)compressor.
 * @author Jagex
 * @author Displee
 */
class BZIP2Compressor : Compressor {
    /**
     * The current bzip2 block entry.
     */
    var bzip2BlockEntry: BZIP2BlockEntry = BZIP2BlockEntry()

    /**
     * An unkown integer array.
     */
    private var anIntArray5786: IntArray = IntArray(100000)

    /**
     * Compress a decompressed BZIP2 file.
     * @param bytes The uncompressed BZIP2 file.
     * @return The compressed BZIP2 file.
     */
    override fun compress(bytes: ByteArray): ByteArray {
        var bytes = bytes
        try {
            ByteArrayInputStream(bytes).use { stream ->
                val bout = ByteArrayOutputStream()
                CBZip2OutputStream(bout, 1).use { os ->
                    val buf = ByteArray(4096)
                    var len: Int
                    while (stream.read(buf, 0, buf.size).also { len = it } != -1) {
                        os.write(buf, 0, len)
                    }
                }
                bytes = bout.toByteArray()
                val bzip2 = ByteArray(bytes.size - 2)
                System.arraycopy(bytes, 2, bzip2, 0, bzip2.size)
                return bzip2
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return byteArrayOf()
        }
    }

    override fun decompress(buffer: InputBuffer, compressedSize: Int, decompressedSize: Int): ByteArray {
        val decompressed = ByteArray(decompressedSize)
        val compressed = buffer.readBytes(compressedSize)
        decompress(decompressed, decompressed.size, compressed, compressedSize, 0)
        return decompressed
    }

    /**
     * Decompress a compressed BZIP2 file.
     * @param decompressed       An empty byte array where we put the decompressed data in.
     * @param decompressedLength The length to decompress.
     * @param archiveData        The compressed BZIP2 file.
     * @param compressedSize     The size of the compressed BZIP2 file.
     * @param startOffset        The start offset.
     * @return The decompressed length.
     */
    fun decompress(decompressed: ByteArray, decompressedLength: Int, archiveData: ByteArray, compressedSize: Int, startOffset: Int): Int {
        synchronized(bzip2BlockEntry) {
            bzip2BlockEntry.compressed = archiveData
            bzip2BlockEntry.startOffset = startOffset
            bzip2BlockEntry.decompressed = decompressed
            bzip2BlockEntry.anInt3100 = 0
            bzip2BlockEntry.decompressedLength = decompressedLength
            bzip2BlockEntry.anInt3088 = 0
            bzip2BlockEntry.anInt3078 = 0
            bzip2BlockEntry.anInt3085 = 0
            bzip2BlockEntry.anInt3097 = 0
            decompress(bzip2BlockEntry)
            bzip2BlockEntry.compressed = ByteArray(0)
            bzip2BlockEntry.decompressed = ByteArray(0)
            return decompressedLength - bzip2BlockEntry.decompressedLength
        }
    }

    /**
     * Decompress a BZIP2 block entry.
     * @param bzip2BlockEntry [BZIP2BlockEntry]
     */
    fun decompress(bzip2BlockEntry: BZIP2BlockEntry) {
        var i: Int
        var is_25_: IntArray
        var is_26_: IntArray
        var is_27_: IntArray
        var bool_28_ = true
        while (bool_28_) {
            var i_29_ = method147(bzip2BlockEntry)
            if (i_29_.toInt() == 23) {
                break
            }
            method147(bzip2BlockEntry)
            method147(bzip2BlockEntry)
            method147(bzip2BlockEntry)
            method147(bzip2BlockEntry)
            method147(bzip2BlockEntry)
            method147(bzip2BlockEntry)
            method147(bzip2BlockEntry)
            method147(bzip2BlockEntry)
            method147(bzip2BlockEntry)
            method153(bzip2BlockEntry)
            bzip2BlockEntry.anInt3083 = 0
            var i_30_ = method147(bzip2BlockEntry).toInt()
            bzip2BlockEntry.anInt3083 = bzip2BlockEntry.anInt3083 shl 8 or (i_30_ and 0xff)
            i_30_ = method147(bzip2BlockEntry).toInt()
            bzip2BlockEntry.anInt3083 = bzip2BlockEntry.anInt3083 shl 8 or (i_30_ and 0xff)
            i_30_ = method147(bzip2BlockEntry).toInt()
            bzip2BlockEntry.anInt3083 = bzip2BlockEntry.anInt3083 shl 8 or (i_30_ and 0xff)
            for (i_31_ in 0..15) {
                i_29_ = method153(bzip2BlockEntry)
                if (i_29_.toInt() == 1) {
                    bzip2BlockEntry.aBooleanArray3072[i_31_] = true
                } else {
                    bzip2BlockEntry.aBooleanArray3072[i_31_] = false
                }
            }
            for (i_32_ in 0..255) {
                bzip2BlockEntry.aBooleanArray3103[i_32_] = false
            }
            for (i_33_ in 0..15) {
                if (bzip2BlockEntry.aBooleanArray3072[i_33_]) {
                    for (i_34_ in 0..15) {
                        i_29_ = method153(bzip2BlockEntry)
                        if (i_29_.toInt() == 1) {
                            bzip2BlockEntry.aBooleanArray3103[i_33_ * 16 + i_34_] = true
                        }
                    }
                }
            }
            method149(bzip2BlockEntry)
            val i_35_ = bzip2BlockEntry.anInt3073 + 2
            val i_36_ = method152(3, bzip2BlockEntry)
            val i_37_ = method152(15, bzip2BlockEntry)
            for (i_38_ in 0 until i_37_) {
                var i_39_ = 0
                while (true) {
                    i_29_ = method153(bzip2BlockEntry)
                    if (i_29_.toInt() == 0) {
                        break
                    }
                    i_39_++
                }
                bzip2BlockEntry.aByteArray3094[i_38_] = i_39_.toByte()
            }
            val is_40_ = ByteArray(6)
            for (i_41_ in 0 until i_36_) {
                is_40_[i_41_] = i_41_.toByte()
            }
            for (i_42_ in 0 until i_37_) {
                var i_43_ = bzip2BlockEntry.aByteArray3094[i_42_]
                val i_44_ = is_40_[i_43_.toInt()]
                while ( /**/i_43_ > 0) {
                    is_40_[i_43_.toInt()] = is_40_[i_43_ - 1]
                    i_43_--
                }
                is_40_[0] = i_44_
                bzip2BlockEntry.aByteArray3076[i_42_] = i_44_
            }
            for (i_45_ in 0 until i_36_) {
                var i_46_ = method152(5, bzip2BlockEntry)
                for (i_47_ in 0 until i_35_) {
                    while (true) {
                        i_29_ = method153(bzip2BlockEntry)
                        if (i_29_.toInt() == 0) {
                            break
                        }
                        i_29_ = method153(bzip2BlockEntry)
                        if (i_29_.toInt() == 0) {
                            i_46_++
                        } else {
                            i_46_--
                        }
                    }
                    bzip2BlockEntry.aByteArrayArray3098[i_45_][i_47_] = i_46_.toByte()
                }
            }
            for (i_48_ in 0 until i_36_) {
                var i_49_ = 32
                var i_50_: Byte = 0
                for (i_51_ in 0 until i_35_) {
                    if (bzip2BlockEntry.aByteArrayArray3098[i_48_][i_51_] > i_50_) {
                        i_50_ = bzip2BlockEntry.aByteArrayArray3098[i_48_][i_51_]
                    }
                    if (bzip2BlockEntry.aByteArrayArray3098[i_48_][i_51_] < i_49_) {
                        i_49_ = bzip2BlockEntry.aByteArrayArray3098[i_48_][i_51_].toInt()
                    }
                }
                method145(bzip2BlockEntry.anIntArrayArray3095[i_48_],
                    bzip2BlockEntry.anIntArrayArray3082[i_48_],
                    bzip2BlockEntry.anIntArrayArray3099[i_48_],
                    bzip2BlockEntry.aByteArrayArray3098[i_48_],
                    i_49_,
                    i_50_.toInt(),
                    i_35_)
                bzip2BlockEntry.anIntArray3090[i_48_] = i_49_
            }
            val i_52_ = bzip2BlockEntry.anInt3073 + 1
            var i_53_ = -1
            for (i_55_ in 0..255) {
                bzip2BlockEntry.anIntArray3075[i_55_] = 0
            }
            var i_56_ = 4095
            for (i_57_ in 15 downTo 0) {
                for (i_58_ in 15 downTo 0) {
                    bzip2BlockEntry.aByteArray3101[i_56_] = (i_57_ * 16 + i_58_).toByte()
                    i_56_--
                }
                bzip2BlockEntry.anIntArray3092[i_57_] = i_56_ + 1
            }
            var i_59_ = 0
            i_53_++
            var i_54_ = 50
            val i_60_ = bzip2BlockEntry.aByteArray3076[i_53_]
            i = bzip2BlockEntry.anIntArray3090[i_60_.toInt()]
            is_25_ = bzip2BlockEntry.anIntArrayArray3095[i_60_.toInt()]
            is_27_ = bzip2BlockEntry.anIntArrayArray3099[i_60_.toInt()]
            is_26_ = bzip2BlockEntry.anIntArrayArray3082[i_60_.toInt()]
            i_54_--
            var i_61_ = i
            var i_62_: Int
            var i_63_: Int
            i_63_ = method152(i_61_, bzip2BlockEntry)
            while (i_63_ > is_25_[i_61_]) {
                i_61_++
                i_62_ = method153(bzip2BlockEntry).toInt()
                i_63_ = i_63_ shl 1 or i_62_
            }
            var i_64_ = is_27_[i_63_ - is_26_[i_61_]]
            while (i_64_ != i_52_) {
                if (i_64_ == 0 || i_64_ == 1) {
                    var i_65_ = -1
                    var i_66_ = 1
                    do {
                        if (i_64_ == 0) {
                            i_65_ += i_66_
                        } else if (i_64_ == 1) {
                            i_65_ += 2 * i_66_
                        }
                        i_66_ *= 2
                        if (i_54_ == 0) {
                            i_53_++
                            i_54_ = 50
                            val i_67_ = bzip2BlockEntry.aByteArray3076[i_53_]
                            i = bzip2BlockEntry.anIntArray3090[i_67_.toInt()]
                            is_25_ = bzip2BlockEntry.anIntArrayArray3095[i_67_.toInt()]
                            is_27_ = bzip2BlockEntry.anIntArrayArray3099[i_67_.toInt()]
                            is_26_ = bzip2BlockEntry.anIntArrayArray3082[i_67_.toInt()]
                        }
                        i_54_--
                        i_61_ = i
                        i_63_ = method152(i_61_, bzip2BlockEntry)
                        while (i_63_ > is_25_[i_61_]) {
                            i_61_++
                            i_62_ = method153(bzip2BlockEntry).toInt()
                            i_63_ = i_63_ shl 1 or i_62_
                        }
                        i_64_ = is_27_[i_63_ - is_26_[i_61_]]
                    } while (i_64_ == 0 || i_64_ == 1)
                    i_65_++
                    i_30_ = bzip2BlockEntry.aByteArray3107[bzip2BlockEntry.aByteArray3101[bzip2BlockEntry.anIntArray3092[0]].toInt() and 0xff].toInt()
                    bzip2BlockEntry.anIntArray3075[i_30_ and 0xff] += i_65_
                    while ( /**/i_65_ > 0) {
                        anIntArray5786[i_59_] = i_30_ and 0xff
                        i_59_++
                        i_65_--
                    }
                } else {
                    var i_68_ = i_64_ - 1
                    if (i_68_ < 16) {
                        val i_69_ = bzip2BlockEntry.anIntArray3092[0]
                        i_29_ = bzip2BlockEntry.aByteArray3101[i_69_ + i_68_]
                        while ( /**/i_68_ > 3) {
                            val i_70_ = i_69_ + i_68_
                            bzip2BlockEntry.aByteArray3101[i_70_] = bzip2BlockEntry.aByteArray3101[i_70_ - 1]
                            bzip2BlockEntry.aByteArray3101[i_70_ - 1] = bzip2BlockEntry.aByteArray3101[i_70_ - 2]
                            bzip2BlockEntry.aByteArray3101[i_70_ - 2] = bzip2BlockEntry.aByteArray3101[i_70_ - 3]
                            bzip2BlockEntry.aByteArray3101[i_70_ - 3] = bzip2BlockEntry.aByteArray3101[i_70_ - 4]
                            i_68_ -= 4
                        }
                        while ( /**/i_68_ > 0) {
                            bzip2BlockEntry.aByteArray3101[i_69_ + i_68_] = bzip2BlockEntry.aByteArray3101[i_69_ + i_68_ - 1]
                            i_68_--
                        }
                        bzip2BlockEntry.aByteArray3101[i_69_] = i_29_
                    } else {
                        var i_71_ = i_68_ / 16
                        val i_72_ = i_68_ % 16
                        var i_73_ = bzip2BlockEntry.anIntArray3092[i_71_] + i_72_
                        i_29_ = bzip2BlockEntry.aByteArray3101[i_73_]
                        while ( /**/i_73_ > bzip2BlockEntry.anIntArray3092[i_71_]) {
                            bzip2BlockEntry.aByteArray3101[i_73_] = bzip2BlockEntry.aByteArray3101[i_73_ - 1]
                            i_73_--
                        }
                        bzip2BlockEntry.anIntArray3092[i_71_]++
                        while ( /**/i_71_ > 0) {
                            bzip2BlockEntry.anIntArray3092[i_71_]--
                            bzip2BlockEntry.aByteArray3101[bzip2BlockEntry.anIntArray3092[i_71_]] = bzip2BlockEntry.aByteArray3101[bzip2BlockEntry.anIntArray3092[i_71_ - 1] + 16 - 1]
                            i_71_--
                        }
                        bzip2BlockEntry.anIntArray3092[0]--
                        bzip2BlockEntry.aByteArray3101[bzip2BlockEntry.anIntArray3092[0]] = i_29_
                        if (bzip2BlockEntry.anIntArray3092[0] == 0) {
                            var i_74_ = 4095
                            for (i_75_ in 15 downTo 0) {
                                for (i_76_ in 15 downTo 0) {
                                    bzip2BlockEntry.aByteArray3101[i_74_] = bzip2BlockEntry.aByteArray3101[bzip2BlockEntry.anIntArray3092[i_75_] + i_76_]
                                    i_74_--
                                }
                                bzip2BlockEntry.anIntArray3092[i_75_] = i_74_ + 1
                            }
                        }
                    }
                    bzip2BlockEntry.anIntArray3075[bzip2BlockEntry.aByteArray3107[i_29_.toInt() and 0xff].toInt() and 0xff]++
                    anIntArray5786[i_59_] = bzip2BlockEntry.aByteArray3107[i_29_.toInt() and 0xff].toInt() and 0xff
                    i_59_++
                    if (i_54_ == 0) {
                        i_53_++
                        i_54_ = 50
                        val i_77_ = bzip2BlockEntry.aByteArray3076[i_53_]
                        i = bzip2BlockEntry.anIntArray3090[i_77_.toInt()]
                        is_25_ = bzip2BlockEntry.anIntArrayArray3095[i_77_.toInt()]
                        is_27_ = bzip2BlockEntry.anIntArrayArray3099[i_77_.toInt()]
                        is_26_ = bzip2BlockEntry.anIntArrayArray3082[i_77_.toInt()]
                    }
                    i_54_--
                    i_61_ = i
                    i_63_ = method152(i_61_, bzip2BlockEntry)
                    while (i_63_ > is_25_[i_61_]) {
                        i_61_++
                        i_62_ = method153(bzip2BlockEntry).toInt()
                        i_63_ = i_63_ shl 1 or i_62_
                    }
                    i_64_ = is_27_[i_63_ - is_26_[i_61_]]
                }
            }
            bzip2BlockEntry.anInt3080 = 0
            bzip2BlockEntry.aByte3108 = 0.toByte()
            bzip2BlockEntry.anIntArray3091[0] = 0
            for (i_78_ in 1..256) {
                bzip2BlockEntry.anIntArray3091[i_78_] = bzip2BlockEntry.anIntArray3075[i_78_ - 1]
            }
            for (i_79_ in 1..256) {
                bzip2BlockEntry.anIntArray3091[i_79_] += bzip2BlockEntry.anIntArray3091[i_79_ - 1]
            }
            for (i_80_ in 0 until i_59_) {
                i_30_ = (anIntArray5786[i_80_] and 0xff).toByte().toInt()
                anIntArray5786[bzip2BlockEntry.anIntArray3091[i_30_ and 0xff]] = anIntArray5786[bzip2BlockEntry.anIntArray3091[i_30_ and 0xff]] or (i_80_ shl 8)
                bzip2BlockEntry.anIntArray3091[i_30_ and 0xff]++
            }
            bzip2BlockEntry.anInt3106 = anIntArray5786[bzip2BlockEntry.anInt3083] shr 8
            bzip2BlockEntry.anInt3071 = 0
            bzip2BlockEntry.anInt3106 = anIntArray5786[bzip2BlockEntry.anInt3106]
            bzip2BlockEntry.anInt3070 = (bzip2BlockEntry.anInt3106 and 0xff).toByte().toInt()
            bzip2BlockEntry.anInt3106 = bzip2BlockEntry.anInt3106 shr 8
            bzip2BlockEntry.anInt3071++
            bzip2BlockEntry.anInt3077 = i_59_
            method151(bzip2BlockEntry)
            bool_28_ = bzip2BlockEntry.anInt3071 == bzip2BlockEntry.anInt3077 + 1 && bzip2BlockEntry.anInt3080 == 0
        }
    }

    fun method152(arg0: Int, arg1: BZIP2BlockEntry): Int {
        val i: Int
        while (true) {
            if (arg1.anInt3088 >= arg0) {
                val i_93_ = arg1.anInt3078 shr arg1.anInt3088 - arg0 and (1 shl arg0) - 1
                arg1.anInt3088 -= arg0
                i = i_93_
                break
            }
            arg1.anInt3078 = arg1.anInt3078 shl 8 or (arg1.compressed[arg1.startOffset].toInt() and 0xff)
            arg1.anInt3088 += 8
            arg1.startOffset++
            arg1.anInt3085++
        }
        return i
    }

    fun method151(arg0: BZIP2BlockEntry) {
        var i = arg0.aByte3108
        var i_81_ = arg0.anInt3080
        var i_82_ = arg0.anInt3071
        var i_83_ = arg0.anInt3070
        val i_90_ = anIntArray5786
        var i_84_ = arg0.anInt3106
        val is_85_ = arg0.decompressed
        var i_86_ = arg0.anInt3100
        var i_87_ = arg0.decompressedLength
        val i_88_ = i_87_
        val i_89_ = arg0.anInt3077 + 1
        while_68_@ while (true) {
            if (i_81_ > 0) {
                while (true) {
                    if (i_87_ == 0) {
                        break@while_68_
                    }
                    if (i_81_ == 1) {
                        break
                    }
                    is_85_[i_86_] = i
                    i_81_--
                    i_86_++
                    i_87_--
                }
                if (i_87_ == 0) {
                    i_81_ = 1
                    break
                }
                is_85_[i_86_] = i
                i_86_++
                i_87_--
            }
            var bool = true
            while (bool) {
                bool = false
                if (i_82_ == i_89_) {
                    i_81_ = 0
                    break@while_68_
                }
                i = i_83_.toByte()
                i_84_ = i_90_[i_84_]
                val i_90_ = (i_84_ and 0xff).toByte().toInt()
                i_84_ = i_84_ shr 8
                i_82_++
                if (i_90_ != i_83_) {
                    i_83_ = i_90_
                    if (i_87_ == 0) {
                        i_81_ = 1
                        break@while_68_
                    }
                    is_85_[i_86_] = i
                    i_86_++
                    i_87_--
                    bool = true
                } else if (i_82_ == i_89_) {
                    if (i_87_ == 0) {
                        i_81_ = 1
                        break@while_68_
                    }
                    is_85_[i_86_] = i
                    i_86_++
                    i_87_--
                    bool = true
                }
            }
            i_81_ = 2
            i_84_ = i_90_[i_84_]
            var i_91_ = (i_84_ and 0xff).toByte().toInt()
            i_84_ = i_84_ shr 8
            if (++i_82_ != i_89_) {
                if (i_91_ != i_83_) {
                    i_83_ = i_91_
                } else {
                    i_81_ = 3
                    i_84_ = i_90_[i_84_]
                    i_91_ = (i_84_ and 0xff).toByte().toInt()
                    i_84_ = i_84_ shr 8
                    if (++i_82_ != i_89_) {
                        if (i_91_ != i_83_) {
                            i_83_ = i_91_
                        } else {
                            i_84_ = i_90_[i_84_]
                            i_91_ = (i_84_ and 0xff).toByte().toInt()
                            i_84_ = i_84_ shr 8
                            i_82_++
                            i_81_ = (i_91_ and 0xff) + 4
                            i_84_ = i_90_[i_84_]
                            i_83_ = (i_84_ and 0xff).toByte().toInt()
                            i_84_ = i_84_ shr 8
                            i_82_++
                        }
                    }
                }
            }
        }
        arg0.anInt3097
        arg0.anInt3097 += i_88_ - i_87_
        arg0.aByte3108 = i
        arg0.anInt3080 = i_81_
        arg0.anInt3071 = i_82_
        arg0.anInt3070 = i_83_
        anIntArray5786 = i_90_
        arg0.anInt3106 = i_84_
        arg0.decompressed = is_85_
        arg0.anInt3100 = i_86_
        arg0.decompressedLength = i_87_
    }

    fun method145(arg0: IntArray, arg1: IntArray, arg2: IntArray, arg3: ByteArray, arg4: Int, arg5: Int, arg6: Int) {
        var i = 0
        for (i_0_ in arg4..arg5) {
            for (i_1_ in 0 until arg6) {
                if (arg3[i_1_].toInt() == i_0_) {
                    arg2[i] = i_1_
                    i++
                }
            }
        }
        for (i_2_ in 0..22) {
            arg1[i_2_] = 0
        }
        for (i_3_ in 0 until arg6) {
            arg1[arg3[i_3_] + 1]++
        }
        for (i_4_ in 1..22) {
            arg1[i_4_] += arg1[i_4_ - 1]
        }
        for (i_5_ in 0..22) {
            arg0[i_5_] = 0
        }
        var i_6_ = 0
        for (i_7_ in arg4..arg5) {
            i_6_ += arg1[i_7_ + 1] - arg1[i_7_]
            arg0[i_7_] = i_6_ - 1
            i_6_ = i_6_ shl 1
        }
        for (i_8_ in arg4 + 1..arg5) {
            arg1[i_8_] = (arg0[i_8_ - 1] + 1 shl 1) - arg1[i_8_]
        }
    }

    fun method147(arg0: BZIP2BlockEntry): Byte {
        return method152(8, arg0).toByte()
    }

    fun method149(arg0: BZIP2BlockEntry) {
        arg0.anInt3073 = 0
        for (i in 0..255) {
            if (arg0.aBooleanArray3103[i]) {
                arg0.aByteArray3107[arg0.anInt3073] = i.toByte()
                arg0.anInt3073++
            }
        }
    }

    fun method153(arg0: BZIP2BlockEntry): Byte {
        return method152(1, arg0).toByte()
    }

    class BZIP2BlockEntry {
        var anInt3070 = 0
        var anInt3071 = 0
        var aBooleanArray3072: BooleanArray = BooleanArray(16)
        var anInt3073 = 0
        var startOffset = 0
        var anIntArray3075: IntArray = IntArray(256)
        var aByteArray3076: ByteArray = ByteArray(18002)
        var anInt3077 = 0
        var anInt3078 = 0
        var decompressed: ByteArray = ByteArray(0)
        var anInt3080 = 0
        var anIntArrayArray3082 = Array(6) { IntArray(258) }
        var anInt3083 = 0
        var anInt3085 = 0
        var anInt3088 = 0
        var anIntArray3090: IntArray = IntArray(6)
        var anIntArray3091: IntArray = IntArray(257)
        var anIntArray3092: IntArray = IntArray(16)
        var decompressedLength = 0
        var aByteArray3094 = ByteArray(18002)
        var anIntArrayArray3095: Array<IntArray> = Array(6) { IntArray(258) }
        var anInt3097 = 0
        var aByteArrayArray3098: Array<ByteArray> = Array(6) { ByteArray(258) }
        var anIntArrayArray3099: Array<IntArray> = Array(6) { IntArray(258) }
        var anInt3100 = 0
        var aByteArray3101: ByteArray = ByteArray(4096)
        var aBooleanArray3103: BooleanArray = BooleanArray(256)
        var anInt3106 = 0
        var aByteArray3107: ByteArray = ByteArray(256)
        var aByte3108: Byte = 0
        var compressed: ByteArray = ByteArray(0)
    }
}