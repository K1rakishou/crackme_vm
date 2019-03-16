package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.core.function.NativeFunctionCallbacks
import crackme.vm.core.function.NativeFunctionType
import crackme.vm.instructions.Call

class CallHandler : Handler<Call>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Call): Int {
    val vmFunction = vm.vmFunctions[instruction.functionName]
    if (vmFunction == null) {
      throw RuntimeException("No function defined with name (${instruction.functionName})")
    }

    vm.vmStack.push64(currentEip + 1L)
    return vmFunction.start
  }

  //TODO: move this too native_call instruction
//  override fun handle(vm: VM, currentEip: Int, instruction: Call): Int {
//    val functionType = NativeFunctionType.fromString(instruction.functionName)
//    if (functionType == null) {
//      throw VmExecutionException(currentEip, "Unknown native function (${instruction.functionName})")
//    }
//
//    val function = NativeFunctionCallbacks.getCallbackByFunctionType(functionType)
//    val parameterTypes = vm.nativeFunctions[functionType]?.variableTypeList
//
//    if (parameterTypes == null) {
//      throw VmExecutionException(currentEip, "Native function definition does not have parameters")
//    }
//
//    vm.registers[0] = function.invoke(vm, parameterTypes)
//    return currentEip + 1
//  }

}