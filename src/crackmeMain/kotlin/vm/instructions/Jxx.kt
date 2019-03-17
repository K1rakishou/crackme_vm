package crackme.vm.instructions

import crackme.vm.core.JumpType

class Jxx(
  val jumpType: JumpType,
  val functionName: String,
  val labelName: String,
  override val instructionType: InstructionType = InstructionType.Jxx
) : Instruction() {
  override fun rawSize(): Int = 1 + 1 + 4 //instructionType + jumpType + instructionId

  override fun compile(): List<ByteArray> {
    val parts = mutableListOf<ByteArray>()

    //FIXME
//    parts += instructionType.value.toByteArray()
//    parts += jumpType.type.toByteArray()
//    parts += labelName.toByteArray()

    return parts
  }

  override fun toString(): String {
    return "${jumpType.jumpName} @$labelName"
  }
}