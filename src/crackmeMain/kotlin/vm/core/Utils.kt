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
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
      val v = (bytes[j] and 0xFF.toByte()).toInt()

      hexChars[j * 2] = hexArray[v ushr 4]
      hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
  }
}