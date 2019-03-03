package crackme.vm.operands

class Variable(
  val name: String,
  val address: Int
) : Operand {
  override val operandName = "Variable"

  override fun toString(): String {
    return "var ($name) at $address"
  }
}