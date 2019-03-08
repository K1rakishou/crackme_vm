package crackme.vm.instructions

import crackme.vm.core.JumpType

class Jxx(
  val jumpType: JumpType,
  val labelName: String,
  override val instructionType: InstructionType = InstructionType.Jxx
) : Instruction() {
  override fun rawSize(): Int = 1 + 1 + 4 //instructionType + jumpType + instructionIndex

  //TODO: rewrite compilation
  override fun compile(): List<ByteArray> {
    val parts = mutableListOf<ByteArray>()

    parts += instructionType.value.toByteArray()
    parts += jumpType.type.toByteArray()
//    parts += instructionIndex.toByteArray()

    return parts
  }

  override fun toString(): String {
    return "${jumpType.jumpName} @$labelName"
  }
}