package crackme.vm.core

import kotlin.experimental.and

object Utils {
  private val hexArray = "0123456789ABCDEF".toCharArray()

  fun copyBytes(from: ByteArray, fromIndex: Int, to: ByteArray, toIndex: Int, count: Int) {
    for (i in 0 until count) {
      to[i + toIndex] = from[i + fromIndex]
    }
  }

  fun bytesToHex(bytes: ByteArray): String {
    return bytes
      .asUByteArray()
      .joinToString("") { it.toString(16).toUpperCase().padStart(2, '0') }
  }

  fun writeShortToArray(offset: Int, value: Short, array: ByteArray) {
    array[offset] = ((value.toInt() shr 8) and 0x000000FF).toByte()
    array[offset + 1] = (value and 0x000000FF).toByte()
  }

  fun readShortFromByteArray(offset: Int, array: ByteArray): Short {
    var result = 0

    for (i in 0..1) {
      result = result shl 8
      result = result or (array[offset + i].toInt() and 0xFF)
    }

    return result.toShort()
  }

  fun writeIntToArray(offset: Int, value: Int, array: ByteArray) {
    array[offset] = ((value shr 24) and 0x000000FF).toByte()
    array[offset + 1] = ((value shr 16) and 0x000000FF).toByte()
    array[offset + 2] = ((value shr 8) and 0x000000FF).toByte()
    array[offset + 3] = (value and 0x000000FF).toByte()
  }

  fun readIntFromArray(offset: Int, array: ByteArray): Int {
    var result = 0

    for (i in 0..3) {
      result = result shl 8
      result = result or (array[offset + i].toInt() and 0xFF)
    }

    return result
  }

  fun writeLongToArray(offset: Int, value: Long, array: ByteArray) {
    array[offset] = ((value shr 56) and 0x000000FF).toByte()
    array[offset + 1] = ((value shr 48) and 0x000000FF).toByte()
    array[offset + 2] = ((value shr 40) and 0x000000FF).toByte()
    array[offset + 3] = ((value shr 32) and 0x000000FF).toByte()
    array[offset + 4] = ((value shr 24) and 0x000000FF).toByte()
    array[offset + 5] = ((value shr 16) and 0x000000FF).toByte()
    array[offset + 6] = ((value shr 8) and 0x000000FF).toByte()
    array[offset + 7] = ((value) and 0x000000FF).toByte()
  }

  fun readLongFromByteArray(offset: Int, array: ByteArray): Long {
    var result = 0L

    for (i in 0..7) {
      result = result shl 8
      result = result or (array[offset + i].toLong() and 0xFF)
    }

    return result
  }
}