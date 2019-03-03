package crackme.vm.instructions

import crackme.vm.core.NativeFunctionType
import crackme.vm.operands.Operand

class Call(
  val type: NativeFunctionType,
  val parameters: List<Operand>
) : Instruction {

  override fun toString(): String {
    return "${type.funcName}(${parameters.joinToString(",")})"
  }
}