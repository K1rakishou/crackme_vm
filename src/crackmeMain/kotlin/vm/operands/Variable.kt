package crackme.vm.operands

import crackme.vm.core.Utils
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
    Utils.writeIntToArray(2, address, array)

    return array
  }

  override fun toString(): String {
    return "$name at $address"
  }
}