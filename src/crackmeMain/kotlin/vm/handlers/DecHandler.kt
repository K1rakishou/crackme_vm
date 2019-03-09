package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Dec

class DecHandler : Handler<Dec>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Dec): Int {
    GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ -> --vm.registers[operand.index] },
      handleMemReg = { operand, _ ->
        val address = vm.registers[operand.operand.index].toInt()
        val oldValue = vm.vmMemory.getLong(address)
        vm.vmMemory.putLong(address, oldValue - 1)
      },
      handleMemVar = { operand, eip ->
        val oldValue = getVmMemoryVariableValue(vm, eip, operand.operand)
        putVmMemoryVariableValue(operand, vm, oldValue - 1, eip)
      },
      handleMemC64 = { operand, _ ->
        val oldValue = vm.vmMemory.getLong(operand.operand.value.toInt())
        vm.vmMemory.putLong(operand.operand.value.toInt(), oldValue - 1)
      },
      handleMemC32 = { operand, _ ->
        val oldValue = vm.vmMemory.getInt(operand.operand.value)
        vm.vmMemory.putInt(operand.operand.value, oldValue - 1)
      }
    )

    //TODO: update flags
    return currentEip + 1
  }

}