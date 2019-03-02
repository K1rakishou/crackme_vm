package crackme.vm.meta

import crackme.vm.core.NativeFunctionType
import crackme.vm.core.ParameterType

class NativeFunction(
  val type: NativeFunctionType,
  val parameterTypeList: List<ParameterType>,
  val nativeFunctionCallback: (List<ParameterType>) -> Long
) : VmParameter