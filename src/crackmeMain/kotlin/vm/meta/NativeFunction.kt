package crackme.vm.meta

import crackme.vm.core.NativeFunctionType
import crackme.vm.core.ParameterType

class NativeFunction(
  val type: NativeFunctionType,
  val parameters: List<ParameterType>
) : VmParameter