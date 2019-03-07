package crackme.vm.operands

class Memory(
  val operand: Operand
) : Operand {
  override val operandName = "Memory"
  override val rawSize: Int = operand.rawSize

  override fun toString(): String {
    return operand.toString()
  }
}