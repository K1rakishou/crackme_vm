package crackme.vm.operands

import crackme.vm.operands.Operand

class Registers(
  val index: Int
) : Operand {

  override fun toString(): String {
    return "r$index"
  }
}