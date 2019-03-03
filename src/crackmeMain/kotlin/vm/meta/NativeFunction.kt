package crackme.vm.meta

import crackme.vm.VM
import crackme.vm.core.NativeFunctionType
import crackme.vm.core.VariableType

class NativeFunction(
  val type: NativeFunctionType,
  val variableTypeList: List<VariableType>,
  val nativeFunctionCallback: (vm: VM, List<VariableType>) -> Long
) : VmParameter