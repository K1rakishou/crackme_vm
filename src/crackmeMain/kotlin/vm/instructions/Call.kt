package crackme.vm.instructions

import crackme.vm.core.function.NativeFunctionType
import crackme.vm.operands.Operand

class Call(
  val functionType: NativeFunctionType,
  val parameters: List<Operand>,
  override val instructionType: InstructionType = InstructionType.Call
) : Instruction() {
  override fun rawSize(): Int = 1 + 1 + parameters.sumBy { it.rawSize } //instructionType + functionType + size of parameters

  override fun compile(): List<ByteArray> {
    val parts = mutableListOf<ByteArray>()

    parts += instructionType.value.toByteArray()
    parts += functionType.index.toByteArray()
    parts += parameters.map { it.compile() }
    return parts
  }

  override fun toString(): String {
    return "call ${functionType.funcName}(${parameters.joinToString(",")})"
  }
}