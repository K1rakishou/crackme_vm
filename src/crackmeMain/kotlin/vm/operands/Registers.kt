package crackme.vm.operands

class Registers(
  val index: Int
) : Operand {

  override fun toString(): String {
    return "r$index"
  }
}