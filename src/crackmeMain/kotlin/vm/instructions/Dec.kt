package crackme.vm.instructions

import crackme.vm.operands.Operand

class Dec(
  override val operand: Operand,
  override val instructionType: InstructionType = InstructionType.Dec
) : GenericOneOperandInstruction, Instruction() {
  override fun rawSize(): Int = 1 + operand.rawSize //instructionType + size of operand

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      operand.compile()
    )
  }

  override fun toString(): String {
    return "dec $operand"
  }
}