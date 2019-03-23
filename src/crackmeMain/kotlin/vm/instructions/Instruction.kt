package crackme.vm.instructions

import crackme.vm.core.Utils

abstract class Instruction {
  abstract val instructionType: InstructionType

  //FIXME: probably we don't need to account the operandType size here
  fun getInstructionRawSize(): Int = 1 + rawSize()  //1 is instruction operandType byte
  abstract fun compile(): List<ByteArray>

  protected abstract fun rawSize(): Int

  protected fun Boolean.toByteArray(): ByteArray {
    val array = ByteArray(1)
    array[0] = if (this) 1 else 0
    return array
  }

  protected fun Byte.toByteArray(): ByteArray {
    val array = ByteArray(1)
    array[0] = this
    return array
  }

  protected fun Short.toByteArray(): ByteArray {
    val array = ByteArray(2)
    Utils.writeShortToArray(0, this, array)

    return array
  }

  protected fun Int.toByteArray(): ByteArray {
    val array = ByteArray(4)
    Utils.writeIntToArray(0, this, array)

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
  Ret(6, "Ret"),
  Xor(7, "Xor"),
  Sub(8, "Sub"),
  Inc(9, "Inc"),
  Dec(10, "Dec"),
  Push(11, "Push"),
  Pop(12, "Pop")
}