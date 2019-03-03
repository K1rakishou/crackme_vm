package crackme.vm.meta

import crackme.vm.core.VariableType

class NativeFunctionParameter(
  val type: VariableType,
  val obj: Any
) {
  override fun toString(): String {
    return type.str
  }
}