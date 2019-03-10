package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Pop

class PopHandler : Handler<Pop>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Pop): Int {
    GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ ->
        vm.registers[operand.index] = vm.vmStack.pop64()
      },
      handleMemReg = { operand, eip ->

        //TODO: extract into a separate method
        when (operand.addressingMode) {
          AddressingMode.ModeByte -> TODO("push8  not implemented yet")
          AddressingMode.ModeWord -> TODO("push16 not implemented yet")
          AddressingMode.ModeDword -> {
            val value = vm.vmStack.pop32()
            putVmMemoryValueByRegister(operand, vm, value.toLong(), eip)
          }
          AddressingMode.ModeQword -> {
            val value = vm.vmStack.pop64()
            putVmMemoryValueByRegister(operand, vm, value, eip)
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
          }
          AddressingMode.ModeQword -> {
            val value = vm.vmStack.pop64()
            putVmMemoryValueByVariable(operand, vm, value, eip)
          }
        }
      },
      handleMemC64 = { operand, _ ->
        putConstantValueIntoMemory(vm, operand.operand, vm.vmStack.pop64())
      },
      handleMemC32 = { operand, _ ->
        putConstantValueIntoMemory(vm, operand.operand, vm.vmStack.pop32().toLong())
      },
      handleC64 = { operand, eip ->
        throw VmExecutionException(eip, "Cannot pop into C64")
      },
      handleC32 = { operand, eip ->
        throw VmExecutionException(eip, "Cannot pop into C32")
      }
    )

    return currentEip + 1
  }

}