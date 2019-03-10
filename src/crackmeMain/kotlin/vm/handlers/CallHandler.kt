package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.core.function.NativeFunctionCallbacks
import crackme.vm.instructions.Call

class CallHandler : Handler<Call>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Call): Int {
    val function = NativeFunctionCallbacks.getCallbackByFunctionType(instruction.functionType)
    val parameterTypes = vm.nativeFunctions[instruction.functionType]?.variableTypeList

    if (parameterTypes == null) {
      throw VmExecutionException(currentEip, "Native function definition does not have parameters")
    }

    vm.registers[0] = function.invoke(vm, parameterTypes)
    return currentEip + 1
  }

}