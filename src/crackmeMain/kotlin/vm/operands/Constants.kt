package crackme.vm.operands

import crackme.vm.core.Utils

abstract class Constant : Operand {

  fun getValue(): Long {
    return when (this) {
      is C32 -> this.value.toLong()
      is C64 -> this.value
      else -> throw NotImplementedError("Not implemented for ${this::class.simpleName}")
    }
  }

}

class C32(
  val value: Int
) : Constant() {
  override val operandName = "C32"
  override val rawSize: Int = 1 + 4 //operandType + value
  override val operandType: OperandType = OperandType.Constant_C32

  override fun compile(): ByteArray {
    val array = ByteArray(rawSize)

    array[0] = operandType.value
    Utils.writeIntToArray(1, value, array)

    return array
  }

  override fun toString(): String {
    return value.toString()
  }
}

class C64(
  val value: Long
) : Constant() {
  override val operandName = "C64"
  override val rawSize: Int = 1 + 8 //operandType + value
  override val operandType: OperandType = OperandType.Constant_C64

  override fun compile(): ByteArray {
    val array = ByteArray(rawSize)

    array[0] = operandType.value
    Utils.writeLongToArray(1, value, array)

    return array
  }

  override fun toString(): String {
    return value.toString()
  }
}