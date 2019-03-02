package crackme.vm.meta

import crackme.vm.core.ParameterType

class NativeFunctionParameter(
  val type: ParameterType,
  val obj: Any
) {
  override fun toString(): String {
    return type.str
  }
}