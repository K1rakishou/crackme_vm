package crackme.vm.instructions

import crackme.vm.operands.Operand
import crackme.vm.operands.Variable

class Let(
  val variable: Variable,
  //TODO: make this Constant instead of Operand?
  val initializer: Operand,
  override val instructionType: InstructionType = InstructionType.Let
) : Instruction() {

  //instructionType + variable size + initializer size
  override fun rawSize(): Int = 1 + variable.rawSize + initializer.rawSize

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