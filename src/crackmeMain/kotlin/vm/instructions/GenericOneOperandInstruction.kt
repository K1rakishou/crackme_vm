package crackme.vm.instructions

import crackme.vm.operands.Operand

interface GenericOneOperandInstruction {
  val operand: Operand
}