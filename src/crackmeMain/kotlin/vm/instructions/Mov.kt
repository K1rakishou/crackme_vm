package crackme.vm.instructions

import crackme.vm.operands.Operand

class Mov(
  val dest: Operand,
  val src: Operand,
  override val instructionType: InstructionType = InstructionType.Mov
) : Instruction() {
  override fun rawSize(): Int = 1 + dest.rawSize + src.rawSize //instructionType + size of two operands

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      dest.compile(),
      src.compile()
    )
  }

  override fun toString(): String {
    return "mov $dest, $src"
  }
}