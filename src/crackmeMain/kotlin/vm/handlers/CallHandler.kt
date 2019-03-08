package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.function.NativeFunctionCallbacks
import crackme.vm.instructions.Call

class CallHandler : Handler<Call>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Call): Int {
    val function = NativeFunctionCallbacks.getCallbackByFunctionType(instruction.functionType)
    vm.registers[0] = function.invoke(vm, instruction.parameters)

    return currentEip + 1
  }

}