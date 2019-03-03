package crackme.vm.operands

class Memory(
  val operand: Operand
) : Operand {
  override val operandName = "Memory"

  override fun toString(): String {
    return operand.toString()
  }
}