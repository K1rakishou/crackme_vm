package crackme.vm.core

import kotlin.random.Random

class VmMemory(private val size: Int,
               private val random: Random) {
  private var eip = 0
  private val variables = mutableMapOf<String, Pair<Int, VariableType>>()
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

  fun getVariable(variableName: String): Pair<Int, VariableType>? {
    return variables[variableName]
  }

  fun allocVariable(variableName: String, variableType: VariableType): Int {
    if (eip < 0 || (eip + VARIABLE_SIZE) > size) {
      throw EipIsOutOfBoundsException(eip, eip + VARIABLE_SIZE)
    }

    val address = eip
    variables.put(variableName, Pair(address, variableType))

    eip += VARIABLE_SIZE
    return address
  }

  private fun allocInt(value: Int): Int {
    if (eip < 0 || (eip + INT_SIZE) > size) {
      throw EipIsOutOfBoundsException(eip, size)
    }

    val address = eip
    Utils.writeIntToArray(eip, value, memory)

    eip += INT_SIZE
    return address
  }

  fun putInt(index: Int, value: Int) {
    if (index < 0 || (index + INT_SIZE) > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    Utils.writeIntToArray(index, value, memory)
  }

  fun getInt(index: Int): Int {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    return Utils.readIntFromArray(index, memory)
  }

  fun putLong(index: Int, value: Long) {
    if (index < 0 || (index + LONG_SIZE) > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    Utils.writeLongToArray(index, value, memory)
  }

  fun allocLong(value: Long): Int {
    if (eip < 0 || (eip + LONG_SIZE) > size) {
      throw EipIsOutOfBoundsException(eip, size)
    }

    val address = eip
    Utils.writeLongToArray(eip, value, memory)

    eip += LONG_SIZE
    return address
  }

  fun getLong(index: Int): Long {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    return Utils.readLongFromByteArray(index, memory)
  }

  fun allocString(value: String): Int {
    if (eip < 0 || (eip + value.length) > size) {
      throw EipIsOutOfBoundsException(eip, size)
    }

    val address = eip

    allocInt(value.length)
    Utils.copyBytes(value.toCharArray().map { it.toByte() }.toByteArray(), 0, memory, eip, value.length)

    eip += value.length
    return address
  }

  fun getString(index: Int): String {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    val length = getInt(index)
    val bytes = ByteArray(length)
    Utils.copyBytes(memory, index + INT_SIZE, bytes, 0, length)

    return String(bytes.map { it.toChar() }.toCharArray())
  }

  class EipIsOutOfBoundsException(val eip: Int, val upper: Int) : Exception("eip is out of bounds (eip = ${eip}, upperBound = ${upper})")
  class VmIndexOutOfBoundsException(val index: Int, val upper: Int) : Exception("eip is out of bounds (index = ${index}, upperBound = ${upper})")

  companion object {
    const val INT_SIZE = 4
    const val LONG_SIZE = 4
    const val VARIABLE_SIZE = 4
  }
}