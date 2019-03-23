package crackme.vm.core

import crackme.vm.VM
import kotlin.random.Random

class VmMemory(
  private val size: Int,
  private val registers: MutableList<Long>,
  private val random: Random
) {

  var ip: Int
    get() {
      return registers[VM.ipRegOffset].toInt()
    }
    private set(value) {
      registers[VM.ipRegOffset] = value.toLong()
    }

  init {
    ip = registers[VM.ipRegOffset].toInt()
  }

  private val variables = mutableMapOf<String, MemoryVariable>()
  private val memory = ByteArray(size) { 0 } //TODO: random.nextBytes(size)

  fun isVariableDefined(variableName: String): Boolean {
    return variables.containsKey(variableName)
  }

  fun slice(startIndex: Int = 0, endIndex: Int = size): ByteArray {
    if (startIndex >= endIndex) {
      throw RuntimeException("startIndex must be less than endIndex (startIndex = ${startIndex}, endIndex = ${endIndex})")
    }

    if (startIndex < 0 || startIndex > size) {
      throw VmIndexOutOfBoundsException(startIndex, size)
    }

    if (endIndex < 0 || endIndex > size) {
      throw VmIndexOutOfBoundsException(startIndex, size)
    }

    val count = endIndex - startIndex
    val byteArray = ByteArray(count)
    Utils.copyBytes(memory, startIndex, byteArray, 0, count)

    return byteArray
  }

  fun putBytes(index: Int, bytes: ByteArray) {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    Utils.copyBytes(bytes, 0, memory, index, bytes.size)
  }

  fun alloc(len: Int): Int {
    if (ip < 0 || (ip + len) > size) {
      throw EipIsOutOfBoundsException(ip, ip + len)
    }

    val address = ip
    ip += len

    return address
  }

  fun getVariable(variableName: String): MemoryVariable? {
    return variables[variableName]
  }

  fun <T : Any> getVariableValue(variableName: String, variableType: VariableType): T {
    val variable = variables[variableName]
    if (variable == null) {
      throw RuntimeException("Unknown variable (${variableName})")
    }

    if (variable.variableType != variableType) {
      throw RuntimeException("Variable types do not match (expected = ${variable.variableType}, actual = ${variableType})")
    }

    return when (variableType) {
      VariableType.IntType -> {
        Utils.readIntFromArray(variable.address, memory) as T
      }
      VariableType.LongType -> {
        Utils.readLongFromByteArray(variable.address, memory) as T
      }
      VariableType.StringType -> {
        val address = Utils.readIntFromArray(variable.address, memory)
        val stringLen = Utils.readIntFromArray(address, memory)

        val array = ByteArray(stringLen)
        Utils.copyBytes(memory, address + INT_SIZE, array, 0, stringLen)

        return String(array.map { it.toChar() }.toCharArray()) as T
      }
    }
  }

  fun allocVariable(variableName: String, variableType: VariableType): Int {
    val variableSize = when (variableType) {
      VariableType.IntType -> 4
      VariableType.LongType -> 8
      VariableType.StringType -> 4
    }

    if (ip < 0 || (ip + variableSize) > size) {
      throw EipIsOutOfBoundsException(ip, ip + variableSize)
    }

    val address = ip
    variables.put(variableName, MemoryVariable(address, variableType))

    when (variableType) {
      VariableType.IntType -> Utils.writeIntToArray(address, 0, memory)
      VariableType.LongType -> Utils.writeLongToArray(address, 0, memory)
      VariableType.StringType -> Utils.writeIntToArray(address, 0, memory)
    }

    ip += variableSize
    return address
  }

  private fun allocInt(value: Int): Int {
    if (ip < 0 || (ip + INT_SIZE) > size) {
      throw EipIsOutOfBoundsException(ip, size)
    }

    val address = ip
    Utils.writeIntToArray(ip, value, memory)

    ip += INT_SIZE
    return address
  }

  fun putByte(index: Int, value: Byte) {
    if (ip < 0 || (ip + BYTE_SIZE) > size) {
      throw EipIsOutOfBoundsException(ip, size)
    }

    memory[index] = value
  }

  fun getByte(index: Int): Byte {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    return memory[index]
  }

  fun putShort(index: Int, value: Short) {
    if (index < 0 || (index + SHORT_SIZE) > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    Utils.writeShortToArray(index, value, memory)
  }

  fun getShort(index: Int): Short {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    return Utils.readShortFromByteArray(index, memory)
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
    if (ip < 0 || (ip + LONG_SIZE) > size) {
      throw EipIsOutOfBoundsException(ip, size)
    }

    val address = ip
    Utils.writeLongToArray(ip, value, memory)

    ip += LONG_SIZE
    return address
  }

  fun getLong(index: Int): Long {
    if (index < 0 || index > size) {
      throw VmIndexOutOfBoundsException(index, size)
    }

    return Utils.readLongFromByteArray(index, memory)
  }

  fun allocString(value: String): Int {
    if (ip < 0 || (ip + value.length) > size) {
      throw EipIsOutOfBoundsException(ip, size)
    }

    val address = allocInt(value.length)
    Utils.copyBytes(value.toCharArray().map { it.toByte() }.toByteArray(), 0, memory, ip, value.length)

    ip += value.length
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

  class MemoryVariable(
    val address: Int,
    val variableType: VariableType
  )

  class EipIsOutOfBoundsException(val ip: Int, val upper: Int) : Exception("ip is out of bounds (ip = ${ip}, upperBound = ${upper})")
  class VmIndexOutOfBoundsException(val index: Int, val upper: Int) : Exception("index is out of bounds (index = ${index}, upperBound = ${upper})")

  companion object {
    const val BYTE_SIZE = 1
    const val SHORT_SIZE = 2
    const val INT_SIZE = 4
    const val LONG_SIZE = 8
  }
}