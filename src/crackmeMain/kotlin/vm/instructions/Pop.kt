package crackme.vm.instructions

import crackme.vm.operands.Operand

class Pop(
  override val operand: Operand,
  override val instructionType: InstructionType = InstructionType.Pop
) : GenericOneOperandInstruction, Instruction() {
  override fun rawSize(): Int = 1 + operand.rawSize //instructionType + size of operand

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      operand.compile()
    )
  }

  override fun toString(): String {
    return "pop $operand"
  }
}