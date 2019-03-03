package crackme.vm.operands

import crackme.vm.core.VariableType

class Variable(
  val name: String,
  val address: Int,
  val type: VariableType
) : Operand {
  override val operandName = "Variable"

  override fun toString(): String {
    return "var ($name) at $address"
  }
}