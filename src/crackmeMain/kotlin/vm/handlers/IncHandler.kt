package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Inc

class IncHandler : Handler<Inc>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Inc): Int {
    val result = GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ ->
        val value = vm.registers[operand.index] + 1
        vm.registers[operand.index] = value
        value
      },
      handleMemReg = { operand, _ ->
        val address = vm.registers[operand.operand.index].toInt()
        val newValue = vm.vmMemory.getLong(address) + 1
        vm.vmMemory.putLong(address, newValue)
        newValue
      },
      handleMemVar = { operand, eip ->
        val newValue = getVmMemoryValueByVariable(vm, eip, operand) + 1
        putVmMemoryValueByVariable(operand, vm, newValue, eip)
        newValue
      },
      handleMemC64 = { operand, _ ->
        val newValue = vm.vmMemory.getLong(operand.operand.value.toInt()) + 1
        vm.vmMemory.putLong(operand.operand.value.toInt(), newValue)
        newValue
      },
      handleMemC32 = { operand, _ ->
        val newValue = vm.vmMemory.getInt(operand.operand.value) + 1
        vm.vmMemory.putInt(operand.operand.value, newValue)
        newValue.toLong()
      },
      handleC64 = { _, eip ->
        throw VmExecutionException(eip, "Cannot inc C64")
      },
      handleC32 = { _, eip ->
        throw VmExecutionException(eip, "Cannot inc C32")
      }
    )

    vm.vmFlags.updateFlagsFromResult(result)
    return currentEip + 1
  }

}