package crackme.vm.core

import crackme.vm.VM
import kotlin.random.Random

class VmStack(
  private val size: Int,
  private val registers: MutableList<Long>,
  private val random: Random
) {
  private var stackBottom = 0
  private val stackTop = stackBottom + size
  private val stack = ByteArray(size) { 0 } //TODO: random.nextBytes(size)

  var sp: Int
    get() {
      return registers[VM.spRegOffset].toInt()
    }
    private set(value) {
      registers[VM.spRegOffset] = value.toLong()
    }

  init {
    stackBottom = registers[VM.spRegOffset].toInt()
    sp = stackBottom
  }

  fun isEmpty() = sp == stackBottom

  fun deallocate(amount: Short) {
    if (sp - amount < stackBottom) {
      throw UnderflowException()
    }

    sp -= amount
  }

  fun push(value: Long, addressingMode: AddressingMode) {
    if (sp + addressingMode.size > stackTop) {
      throw OverflowException()
    }

    when (addressingMode) {
      AddressingMode.ModeByte -> stack[sp] = (value and 0xFF).toByte()
      AddressingMode.ModeWord -> Utils.writeShortToArray(sp, value.toShort(), stack)
      AddressingMode.ModeDword -> Utils.writeIntToArray(sp, value.toInt(), stack)
      AddressingMode.ModeQword -> Utils.writeLongToArray(sp, value, stack)
    }

    sp += addressingMode.size
  }

  fun <T : Any> pop(addressingMode: AddressingMode): T {
    if (sp - addressingMode.size < stackBottom) {
      throw UnderflowException()
    }

    sp -= addressingMode.size

    return when (addressingMode) {
      AddressingMode.ModeByte -> stack[sp] as T
      AddressingMode.ModeWord -> Utils.readShortFromByteArray(sp, stack) as T
      AddressingMode.ModeDword -> Utils.readIntFromArray(sp, stack) as T
      AddressingMode.ModeQword -> Utils.readLongFromByteArray(sp, stack) as T
    }
  }

  fun peek8(): Byte {
    return stack[sp]
  }

  fun peek8At(address: Int): Byte {
    if (address < 0) {
      throw UnderflowException()
    }

    if ((address - BYTE_SIZE) >= stackTop) {
      throw OverflowException()
    }

    return stack[address]
  }

  fun peek16(): Short {
    return Utils.readShortFromByteArray(sp - SHORT_SIZE, stack)
  }

  fun peek16At(address: Int): Short {
    if (address < 0) {
      throw UnderflowException()
    }

    if ((address - SHORT_SIZE) >= stackTop) {
      throw OverflowException()
    }

    return Utils.readShortFromByteArray(address, stack)
  }

  fun peek32(): Int {
    return Utils.readIntFromArray(sp - INT_SIZE, stack)
  }

  fun peek32At(address: Int): Int {
    if (address < 0) {
      throw UnderflowException()
    }

    if ((address - INT_SIZE) >= stackTop) {
      throw OverflowException()
    }

    return Utils.readIntFromArray(address, stack)
  }

  fun peek64(): Long {
    return Utils.readLongFromByteArray(sp - LONG_SIZE, stack)
  }

  fun peek64At(address: Int): Long {
    if (address < 0) {
      throw UnderflowException()
    }

    if ((address - LONG_SIZE) >= stackTop) {
      throw OverflowException()
    }

    return Utils.readLongFromByteArray(address, stack)
  }

  fun set8(value: Byte) {
    stack[sp] = value
  }

  fun set8At(address: Int, value: Byte) {
    if (address < 0) {
      throw UnderflowException()
    }

    if ((address - BYTE_SIZE) >= stackTop) {
      throw OverflowException()
    }

    stack[address] = value
  }

  fun set16(value: Short) {
    Utils.writeShortToArray(sp, value, stack)
  }

  fun set16At(address: Int, value: Short) {
    if (address < 0) {
      throw UnderflowException()
    }

    if ((address - SHORT_SIZE) >= stackTop) {
      throw OverflowException()
    }

    Utils.writeShortToArray(address, value, stack)
  }

  fun set32(value: Int) {
    Utils.writeIntToArray(sp, value, stack)
  }

  fun set32At(address: Int, value: Int) {
    if (address < 0) {
      throw UnderflowException()
    }

    if ((address - INT_SIZE) >= stackTop) {
      throw OverflowException()
    }

    Utils.writeIntToArray(address, value, stack)
  }

  fun set64(value: Long) {
    Utils.writeLongToArray(sp, value, stack)
  }

  fun set64At(address: Int, value: Long) {
    if (address < 0) {
      throw UnderflowException()
    }

    if ((address - LONG_SIZE) >= stackTop) {
      throw OverflowException()
    }

    Utils.writeLongToArray(address, value, stack)
  }

  class OverflowException : Exception("Stack overflow")
  class UnderflowException : Exception("Stack is empty")

  companion object {
    const val BYTE_SIZE = 1
    const val SHORT_SIZE = 2
    const val INT_SIZE = 4
    const val LONG_SIZE = 8
  }
}