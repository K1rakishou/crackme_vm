package crackme.vm.instructions

class Ret(
  val bytesToClearFromStack: Short,
  val isVmExit: Boolean,
  override val instructionType: InstructionType = InstructionType.Ret
) : Instruction() {
  override fun rawSize(): Int = 1 + bytesToClearFromStack + 1 //instructionType + bytesToClearFromStack + isVmExit

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      bytesToClearFromStack.toByteArray(),
      isVmExit.toByteArray()
    )
  }

  override fun toString(): String {
    val name = if (!isVmExit) {
      "ret"
    } else {
      "vm_exit"
    }

    return "$name $bytesToClearFromStack"
  }
}