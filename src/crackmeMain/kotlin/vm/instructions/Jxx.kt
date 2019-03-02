package crackme.vm.instructions

import crackme.vm.core.JumpType

class Jxx(
  val jumpType: JumpType,
  val instructionIndex: Int
) : Instruction {

  override fun toString(): String {
    return "${jumpType.type} @$instructionIndex"
  }
}