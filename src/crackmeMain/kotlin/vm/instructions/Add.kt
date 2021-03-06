package crackme.vm.instructions

import crackme.vm.operands.Operand

class Add(
  override val dest: Operand,
  override val src: Operand,
  override val instructionType: InstructionType = InstructionType.Add
) : GenericTwoOperandsInstruction, Instruction() {
  override fun rawSize(): Int = 1 + dest.rawSize + src.rawSize //instructionType + size of two operands

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      dest.compile(),
      src.compile()
    )
  }

  override fun toString(): String {
    return "add $dest, $src"
  }
}