package crackme.vm.operands

import crackme.vm.core.VariableType

class Variable(
  val name: String,
  val address: Int,
  val variableType: VariableType
) : Operand {
  override val operandName = "Variable"
  override val rawSize = 4 + 1 + 1 //address + operandType + variableType
  override val operandType: OperandType = OperandType.Variable

  override fun compile(): ByteArray {
    val array = ByteArray(rawSize)

    array[0] = operandType.value
    array[1] = variableType.type
    array[2] = ((address shr 24) and 0x000000FF).toByte()
    array[3] = ((address shr 16) and 0x000000FF).toByte()
    array[4] = ((address shr 8) and 0x000000FF).toByte()
    array[5] = (address and 0x000000FF).toByte()

    return array
  }

  override fun toString(): String {
    return "var ($name) at $address"
  }
}