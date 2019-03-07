package crackme.vm.operands

class Register(
  val index: Int
) : Operand {
  override val operandName = "Register"
  override val rawSize = 1 //index

  override fun toString(): String {
    return "r$index"
  }
}