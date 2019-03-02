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

  class OutOfBoundsException(val index: Int, val upper: Int) : Exception("index is out of bounds (index = ${index}, upperBound = ${upper})")
}