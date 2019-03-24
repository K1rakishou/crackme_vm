package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Push

class PushHandler : Handler<Push>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Push): Int {
    GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ ->
        vm.vmStack.push(vm.registers[operand.index], instruction.addressingMode)

        0
      },
      handleMemReg = { operand, eip ->
        val value = getVmMemoryValueByRegister(vm, eip, operand)
        vm.vmStack.push(value, instruction.addressingMode)

        0
      },
      handleMemConstant = { operand, eip ->
        val value = getConstantValueFromVmMemory(vm, operand.operand)
        vm.vmStack.push(value, instruction.addressingMode)

        0
      },
      handleC64 = { operand, eip ->
        vm.vmStack.push(operand.value, instruction.addressingMode)

        0
      },
      handleC32 = { operand, eip ->
        vm.vmStack.push(operand.value.toLong(), instruction.addressingMode)

        0
      }
    )

    //we do need to update flags here because this instruction does not change registers
    return currentEip + 1
  }

}