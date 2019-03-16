package crackme.vm.core

import kotlin.random.Random

class VmStack(
  private val size: Int,
  private val random: Random
) {
  var sp: Int = 0
    private set
  private val stack = ByteArray(size) { 0 } //TODO: random.nextBytes(size)

  fun isEmpty() = sp == 0

  fun push64(value: Long) {
    if (sp + LONG_SIZE > size) {
      throw OverflowException()
    }

    Utils.writeLongToArray(sp, value, stack)
    sp += LONG_SIZE
  }

  fun push32(value: Int) {
    if (sp + INT_SIZE > size) {
      throw OverflowException()
    }

    Utils.writeIntToArray(sp, value, stack)
    sp += INT_SIZE
  }

  fun pop64(): Long {
    if (sp - LONG_SIZE < 0) {
      throw UnderflowException()
    }

    sp -= LONG_SIZE
    val value = Utils.readLongFromByteArray(sp, stack)

    return value
  }

  fun pop32(): Int {
    if (sp - INT_SIZE < 0) {
      throw UnderflowException()
    }

    sp -= INT_SIZE
    val value = Utils.readIntFromArray(sp, stack)

    return value
  }

  fun peek64(): Long {
    return Utils.readLongFromByteArray(sp - LONG_SIZE, stack)
  }

  fun peek32(): Int {
    return Utils.readIntFromArray(sp - INT_SIZE, stack)
  }

  //TODO: maybe push/pop/peek 16 and 8

  class OverflowException : Exception("Stack overflow")
  class UnderflowException : Exception("Stack is empty")

  companion object {
    const val BYTE_SIZE = 1
    const val SHORT_SIZE = 2
    const val INT_SIZE = 4
    const val LONG_SIZE = 8
  }
}