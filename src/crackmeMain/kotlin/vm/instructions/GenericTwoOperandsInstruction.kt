package crackme.vm.instructions

import crackme.vm.operands.Operand

interface GenericTwoOperandsInstruction {
  val dest: Operand
  val src: Operand
}