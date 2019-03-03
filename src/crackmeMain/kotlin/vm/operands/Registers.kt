package crackme.vm.operands

class Register(
  val index: Int
) : Operand {
  override val name = "Register"

  override fun toString(): String {
    return "r$index"
  }
}