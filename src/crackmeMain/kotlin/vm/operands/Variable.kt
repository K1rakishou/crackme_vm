package crackme.vm.operands

import crackme.vm.core.VariableType

class Variable(
  val name: String,
  val address: Int,
  val type: VariableType
) : Operand {
  override val operandName = "Variable"
  override val rawSize = 4 + 1 //address and type

  override fun toString(): String {
    return "var ($name) at $address"
  }
}