package crackme.vm.core

import kotlin.random.Random

class VmMemory(private val size: Int,
               private val random: Random) {
  private var eip = 0
  private val variables = mutableMapOf<String, Int>()
  private val memory = random.nextBytes(size)

  fun isVariableDefined(variableName: String): Boolean {
    return variables.containsKey(variableName)
  }

  fun alloc(len: Int): Int {
    if (eip < 0 || (eip + len) > size) {
      throw EipIsOutOfBoundsException(eip, eip + len)
    }

    val address = eip
    eip += len

    return address
  }

  fun allocVariable(variableName: String): Int {
    if (eip < 0 || (eip + VARIABLE_SIZE) > size) {
      throw EipIsOutOfBoundsException(eip, eip + VARIABLE_SIZE)
    }

    val address = eip
    variables.put(variableName, address)

    eip += VARIABLE_SIZE
    return address
  }

  private fun allocInt(value: Int): Int {
    if (eip < 0 || (eip + INT_SIZE) > size) {
      throw EipIsOutOfBoundsException(eip, size)
    }

    val address = eip

    memory[eip] = ((value shr 24) and 0x000000FF).toByte()
    memory[eip + 1] = ((value shr 16) and 0x000000FF).toByte()
    memory[eip + 2] = ((value shr 8) and 0x000000FF).toByte()
    memory[eip + 3] = (value and 0x000000FF).toByte()

    eip += INT_SIZE
    return address
  }

  private fun putInt(index: Int, value: Int) {
    if (index < 0 || (index + INT_SIZE) > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    memory[index] = ((value shr 24) and 0x000000FF).toByte()
    memory[index + 1] = ((value shr 16) and 0x000000FF).toByte()
    memory[index + 2] = ((value shr 8) and 0x000000FF).toByte()
    memory[index + 3] = (value and 0x000000FF).toByte()
  }

  private fun getInt(index: Int): Int {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    var result = 0

    for (i in 0..3) {
      result = result shl 8
      result = result or (memory[index + i].toInt() and 0xFF)
    }

    return result
  }

  fun putLong(index: Int, value: Long) {
    if (index < 0 || (index + LONG_SIZE) > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    memory[index] = ((value shr 56) and 0x000000FF).toByte()
    memory[index + 1] = ((value shr 48) and 0x000000FF).toByte()
    memory[index + 2] = ((value shr 40) and 0x000000FF).toByte()
    memory[index + 3] = ((value shr 32) and 0x000000FF).toByte()
    memory[index + 4] = ((value shr 24) and 0x000000FF).toByte()
    memory[index + 5] = ((value shr 16) and 0x000000FF).toByte()
    memory[index + 6] = ((value shr 8) and 0x000000FF).toByte()
    memory[index + 7] = ((value) and 0x000000FF).toByte()
  }

  fun allocLong(value: Long): Int {
    if (eip < 0 || (eip + LONG_SIZE) > size) {
      throw EipIsOutOfBoundsException(eip, size)
    }

    val address = eip

    memory[eip] = ((value shr 56) and 0x000000FF).toByte()
    memory[eip + 1] = ((value shr 48) and 0x000000FF).toByte()
    memory[eip + 2] = ((value shr 40) and 0x000000FF).toByte()
    memory[eip + 3] = ((value shr 32) and 0x000000FF).toByte()
    memory[eip + 4] = ((value shr 24) and 0x000000FF).toByte()
    memory[eip + 5] = ((value shr 16) and 0x000000FF).toByte()
    memory[eip + 6] = ((value shr 8) and 0x000000FF).toByte()
    memory[eip + 7] = ((value) and 0x000000FF).toByte()

    eip += LONG_SIZE
    return address
  }

  fun getLong(index: Int): Long {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    var result = 0L

    for (i in 0..7) {
      result = result shl 8
      result = result or (memory[index + i].toLong() and 0xFF)
    }

    return result
  }

  fun allocString(value: String): Int {
    if (eip < 0 || (eip + value.length) > size) {
      throw EipIsOutOfBoundsException(eip, size)
    }

    val address = eip

    allocInt(value.length)
    copyBytes(value.toCharArray().map { it.toByte() }.toByteArray(), 0, memory, eip, value.length)

    return address
  }

  fun getString(index: Int): String {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    val length = getInt(index)
    val bytes = ByteArray(length)
    copyBytes(memory, index + INT_SIZE, bytes, 0, length)

    return String(bytes.map { it.toChar() }.toCharArray())
  }

  private fun copyBytes(from: ByteArray, fromIndex: Int, to: ByteArray, toIndex: Int, count: Int) {
    for (i in 0 until count) {
      to[i + toIndex] = from[i + fromIndex]
    }

    eip += count
  }

  class EipIsOutOfBoundsException(val eip: Int, val upper: Int) : Exception("eip is out of bounds (eip = ${eip}, upperBound = ${upper})")
  class VmIndexOutOfBoundsException(val index: Int, val upper: Int) : Exception("eip is out of bounds (index = ${index}, upperBound = ${upper})")

  companion object {
    const val INT_SIZE = 4
    const val LONG_SIZE = 4
    const val VARIABLE_SIZE = 4
  }
}