package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Push

class PushHandler : Handler<Push>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Push): Int {
    GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ ->
        vm.vmStack.push64(vm.registers[operand.index])
      },
      handleMemReg = { operand, eip ->
        val value = getVmMemoryValueByRegister(vm, eip, operand)

        //TODO: extract into a separate method
        when (operand.addressingMode) {
          AddressingMode.ModeByte -> TODO("push8  not implemented yet")
          AddressingMode.ModeWord -> TODO("push16 not implemented yet")
          AddressingMode.ModeDword -> vm.vmStack.push32(value.toInt())
          AddressingMode.ModeQword -> vm.vmStack.push64(value)
        }
      },
      handleMemVar = { operand, eip ->
        val value = getVmMemoryValueByVariable(vm, eip, operand)

        //TODO: extract into a separate method
        when (operand.addressingMode) {
          AddressingMode.ModeByte -> TODO("push8  not implemented yet")
          AddressingMode.ModeWord -> TODO("push16 not implemented yet")
          AddressingMode.ModeDword -> vm.vmStack.push32(value.toInt())
          AddressingMode.ModeQword -> vm.vmStack.push64(value)
        }
      },
      handleMemC64 = { operand, _ ->
        vm.vmStack.push64(getConstantValueFromVmMemory(vm, operand.operand))
      },
      handleMemC32 = { operand, _ ->
        vm.vmStack.push32(getConstantValueFromVmMemory(vm, operand.operand).toInt())
      },
      handleC64 = { operand, eip ->
        vm.vmStack.push64(operand.value)
      },
      handleC32 = { operand, eip ->
        //TODO addressing mode for push/pop
        //push64 here because C64 is the default constant size
        vm.vmStack.push64(operand.value.toLong())
      }
    )

    return currentEip + 1
  }

}