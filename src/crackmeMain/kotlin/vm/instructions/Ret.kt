package crackme.vm.instructions

class Ret(
  override val instructionType: InstructionType = InstructionType.Ret
) : Instruction() {
  override fun rawSize(): Int = 1 //instructionType

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray()
    )
  }

  override fun toString(): String {
    return "ret"
  }
}