package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.NativeFunctionCallbacks
import crackme.vm.instructions.Call

class CallHandler : Handler<Call> {

  override fun handle(vm: VM, eip: Int, instruction: Call) {
    val function = NativeFunctionCallbacks.getCallbackByFunctionType(instruction.type)
    vm.registers[0] = function.invoke(vm, instruction.parameters)
  }

}