package crackme.vm.instructions

class Ret(
  val amountToDeallocate: Short,
  val isVmExit: Boolean,
  override val instructionType: InstructionType = InstructionType.Ret
) : Instruction() {
  override fun rawSize(): Int = 1 + amountToDeallocate + 1 //instructionType + amountToDeallocate + isVmExit

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      amountToDeallocate.toByteArray(),
      isVmExit.toByteArray()
    )
  }

  override fun toString(): String {
    val name = if (!isVmExit) {
      "ret"
    } else {
      "vm_exit"
    }

    return "$name $amountToDeallocate"
  }
}