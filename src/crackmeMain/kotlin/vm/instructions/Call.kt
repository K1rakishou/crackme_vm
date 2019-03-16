package crackme.vm.instructions

import crackme.vm.core.function.NativeFunctionType

class Call(
  val functionName: String,
  override val instructionType: InstructionType = InstructionType.Call
) : Instruction() {
  override fun rawSize(): Int = 1 + 1 //instructionType + functionType

  override fun compile(): List<ByteArray> {
    val parts = mutableListOf<ByteArray>()

    //FIXME
//    parts += instructionType.value.toByteArray()
//    parts += functionType.index.toByteArray()
    return parts
  }

  override fun toString(): String {
    return "call ${functionName}"
  }
}