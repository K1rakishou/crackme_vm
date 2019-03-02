package crackme.vm.instructions

import crackme.vm.operands.Operand

class Ret(
  val result: Operand
) : Instruction {

  override fun toString(): String {
    return "ret $result"
  }
}