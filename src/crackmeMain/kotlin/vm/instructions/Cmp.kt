package crackme.vm.instructions

import crackme.vm.operands.Operand

class Cmp(
  val dest: Operand,
  val src: Operand
) : Instruction {

  override fun toString(): String {
    return "cmp $dest, $src"
  }
}
