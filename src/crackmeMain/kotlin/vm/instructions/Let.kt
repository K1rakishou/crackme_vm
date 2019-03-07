package crackme.vm.instructions

import crackme.vm.operands.Operand
import crackme.vm.operands.Variable

class Let(
  val variable: Variable,
  val initializer: Operand
) : Instruction() {
  override fun rawSize(): Int = variable.rawSize + initializer.rawSize

  override fun toString(): String {
    return "let ${variable.name}, $initializer"
  }
}