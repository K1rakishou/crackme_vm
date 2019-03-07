package crackme.vm.operands

abstract class Constant : Operand

class C64(
  val value: Long
) : Constant() {
  override val operandName = "C64"
  override val rawSize: Int = 1 + 8 //operandType + value
  override val operandType: OperandType = OperandType.Constant_C64

  override fun compile(): ByteArray {
    val array = ByteArray(rawSize)

    array[0] = operandType.value
    array[1] = ((value shr 56) and 0x000000FF).toByte()
    array[2] = ((value shr 48) and 0x000000FF).toByte()
    array[3] = ((value shr 40) and 0x000000FF).toByte()
    array[4] = ((value shr 32) and 0x000000FF).toByte()
    array[5] = ((value shr 24) and 0x000000FF).toByte()
    array[6] = ((value shr 16) and 0x000000FF).toByte()
    array[7] = ((value shr 8) and 0x000000FF).toByte()
    array[8] = ((value) and 0x000000FF).toByte()

    return array
  }

  override fun toString(): String {
    return value.toString()
  }
}

class VmString(
  val address: Int
) : Constant() {
  override val operandName = "String"
  override val rawSize: Int = 1 + 4 //operandType + address
  override val operandType: OperandType = OperandType.Constant_VmString

  override fun compile(): ByteArray {
    val array = ByteArray(rawSize)

    array[0] = operandType.value
    array[1] = ((address shr 24) and 0x000000FF).toByte()
    array[2] = ((address shr 16) and 0x000000FF).toByte()
    array[3] = ((address shr 8) and 0x000000FF).toByte()
    array[4] = (address and 0x000000FF).toByte()

    return array
  }

  override fun toString(): String {
    return "string at ($address)"
  }
}