package crackme.vm.instructions

abstract class Instruction {
  abstract val instructionType: InstructionType

  fun getInstructionRawSize(): Int = 1 + rawSize()  //1 is instruction operandType byte
  abstract fun compile(): List<ByteArray>

  protected abstract fun rawSize(): Int

  protected fun Byte.toByteArray(): ByteArray {
    val array = ByteArray(1)
    array[0] = this
    return array
  }

  protected fun Int.toByteArray(): ByteArray {
    val array = ByteArray(4)

    array[0] = ((this shr 24) and 0x000000FF).toByte()
    array[1] = ((this shr 16) and 0x000000FF).toByte()
    array[2] = ((this shr 8) and 0x000000FF).toByte()
    array[3] = (this and 0x000000FF).toByte()

    return array
  }
}

enum class InstructionType(
  val value: Byte,
  val instructionName: String
) {
  Add(0, "Add"),
  Call(1, "Call"),
  Cmp(2, "Cmp"),
  Jxx(3, "Jxx"),
  Let(4, "Let"),
  Mov(5, "Mov"),
  Ret(6, "Ret")
}