package crackme.vm.meta

import crackme.vm.VM
import crackme.vm.core.NativeFunctionType
import crackme.vm.core.ParameterType

class NativeFunction(
  val type: NativeFunctionType,
  val parameterTypeList: List<ParameterType>,
  val nativeFunctionCallback: (vm: VM, List<ParameterType>) -> Long
) : VmParameter