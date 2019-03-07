package crackme.vm.instructions

import crackme.vm.core.function.NativeFunctionType
import crackme.vm.operands.Operand

class Call(
  val functionType: NativeFunctionType,
  val parameters: List<Operand>
) : Instruction() {
  override fun rawSize(): Int = 1 //functionType

  override fun toString(): String {
    return "call ${functionType.funcName}(${parameters.joinToString(",")})"
  }
}