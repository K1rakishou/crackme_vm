package crackme.vm.core

import kotlin.random.Random

class VmMemory(private val size: Int,
               private val random: Random) {
  private val memory = random.nextBytes(size)

  fun putInt(index: Int, value: Int) {
    if (index < 0 || index > size) {
      throw OutOfBoundsException(index, size)
    }

    memory[index] = ((value shr 24) and 0x000000FF).toByte()
    memory[index + 1] = ((value shr 16) and 0x000000FF).toByte()
    memory[index + 2] = ((value shr 8) and 0x000000FF).toByte()
    memory[index + 3] = (value and 0x000000FF).toByte()
  }

  fun getInt(index: Int): Int {
    if (index < 0 || index > size) {
      throw OutOfBoundsException(index, size)
    }

    var result = 0

    for (i in 0..3) {
      result = result shl 8
      result = result or (memory[index + i].toInt() and 0xFF)
    }

    return result
  }

  fun putString(index: Int, value: String) {
    if (index < 0 || (index + value.length) > size) {
      throw OutOfBoundsException(index, size)
    }

    copyBytes(value.toCharArray().map { it.toByte() }.toByteArray(), 0, memory, index, value.length)
  }

  fun getString(index: Int, length: Int): String {
    if (index < 0 || (index + length) > size) {
      throw OutOfBoundsException(index, size)
    }

    val bytes = ByteArray(length)
    copyBytes(memory, index, bytes, 0, length)

    return String(bytes.map { it.toChar() }.toCharArray())
  }

  private fun copyBytes(from: ByteArray, fromIndex: Int, to: ByteArray, toIndex: Int, count: Int) {
    for (i in 0 until count) {
      to[i + toIndex] = from[i + fromIndex]
    }
  }

  class OutOfBoundsException(val index: Int, val upper: Int) : Exception("index is out of bounds (index = ${index}, upperBound = ${upper})")
}