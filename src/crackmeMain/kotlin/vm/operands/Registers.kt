package crackme.vm.operands

class Register(
  val index: Int
) : Operand {
  override val operandName = "Register"

  override fun toString(): String {
    return "r$index"
  }
}