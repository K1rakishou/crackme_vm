package crackme.vm.instructions

import crackme.vm.operands.Operand

class Mov(
  val dest: Operand,
  val src: Operand
) : Instruction() {
  override fun rawSize(): Int = dest.rawSize + src.rawSize

  override fun toString(): String {
    return "mov $dest, $src"
  }
}