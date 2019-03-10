package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Pop

class PopHandler : Handler<Pop>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Pop): Int {
    val result = GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ ->
        val value = vm.vmStack.pop64()
        vm.registers[operand.index] = value
        value
      },
      handleMemReg = { operand, eip ->

        //TODO: extract into a separate method
        when (operand.addressingMode) {
          AddressingMode.ModeByte -> TODO("push8  not implemented yet")
          AddressingMode.ModeWord -> TODO("push16 not implemented yet")
          AddressingMode.ModeDword -> {
            val value = vm.vmStack.pop32()
            putVmMemoryValueByRegister(operand, vm, value.toLong(), eip)
            value.toLong()
          }
          AddressingMode.ModeQword -> {
            val value = vm.vmStack.pop64()
            putVmMemoryValueByRegister(operand, vm, value, eip)
            value
          }
        }
      },
      handleMemVar = { operand, eip ->
        //TODO: extract into a separate method
        when (operand.addressingMode) {
          AddressingMode.ModeByte -> TODO("push8  not implemented yet")
          AddressingMode.ModeWord -> TODO("push16 not implemented yet")
          AddressingMode.ModeDword -> {
            val value = vm.vmStack.pop32()
            putVmMemoryValueByVariable(operand, vm, value.toLong(), eip)
            value.toLong()
          }
          AddressingMode.ModeQword -> {
            val value = vm.vmStack.pop64()
            putVmMemoryValueByVariable(operand, vm, value, eip)
            value
          }
        }
      },
      handleMemC64 = { operand, _ ->
        val value = vm.vmStack.pop64()
        putConstantValueIntoMemory(vm, operand.operand, value)
        value
      },
      handleMemC32 = { operand, _ ->
        val value = vm.vmStack.pop32().toLong()
        putConstantValueIntoMemory(vm, operand.operand, value)
        value
      },
      handleC64 = { operand, eip ->
        throw VmExecutionException(eip, "Cannot pop into C64")
      },
      handleC32 = { operand, eip ->
        throw VmExecutionException(eip, "Cannot pop into C32")
      }
    )

    vm.vmFlags.updateFlagsFromResult(result)
    return currentEip + 1
  }

}