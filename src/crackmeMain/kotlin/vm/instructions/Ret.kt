package crackme.vm.instructions

import crackme.vm.operands.Register

class Ret(
  val result: Register,
  override val instructionType: InstructionType = InstructionType.Ret
) : Instruction() {
  override fun rawSize(): Int = 1 + result.rawSize //instructionType + size of operand

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      result.compile()
    )
  }

  override fun toString(): String {
    return "ret $result"
  }
}