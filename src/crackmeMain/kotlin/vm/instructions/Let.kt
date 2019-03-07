package crackme.vm.instructions

import crackme.vm.operands.Operand
import crackme.vm.operands.Variable

class Let(
  val variable: Variable,
  val initializer: Operand,
  override val instructionType: InstructionType = InstructionType.Let
) : Instruction() {
  override fun rawSize(): Int = 1 + variable.rawSize + initializer.rawSize //instructionType + variable size + initializer size

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      variable.compile(),
      initializer.compile()
    )
  }

  override fun toString(): String {
    return "let ${variable.name}, $initializer"
  }
}