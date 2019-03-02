package crackme.vm.instructions

import crackme.vm.core.NativeFunctionType
import crackme.vm.meta.NativeFunctionParameter

class Call(
  val type: NativeFunctionType,
  val parameters: List<NativeFunctionParameter>
) : Instruction {

  override fun toString(): String {
    return "${type.funcName}(${parameters.joinToString(",")})"
  }
}