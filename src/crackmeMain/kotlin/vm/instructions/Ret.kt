package crackme.vm.instructions

class Ret(
  val bytesToClearFromStack: Short,
  override val instructionType: InstructionType = InstructionType.Ret
) : Instruction() {
  override fun rawSize(): Int = 1 + bytesToClearFromStack //instructionType + bytesToClearFromStack

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      bytesToClearFromStack.toByteArray()
    )
  }

  override fun toString(): String {
    return "ret $bytesToClearFromStack"
  }
}