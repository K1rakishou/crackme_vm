package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Dec

class DecHandler : Handler<Dec>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Dec): Int {
    val result = GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ -> --vm.registers[operand.index] },
      handleMemReg = { operand, _ ->
        //FIXME: probably this wont work
        val address = vm.registers[operand.operand.index].toInt()
        val newValue = vm.vmMemory.getLong(address) - 1
        vm.vmMemory.putLong(address, newValue)
        newValue
      },
      handleMemVar = { operand, eip ->
        val newValue = getVmMemoryValueByVariable(vm, eip, operand) - 1
        putVmMemoryValueByVariable(operand, vm, newValue, eip)
        newValue
      },
      handleMemC64 = { operand, _ ->
        val newValue = vm.vmMemory.getLong(operand.operand.value.toInt()) - 1
        vm.vmMemory.putLong(operand.operand.value.toInt(), newValue)
        newValue
      },
      handleMemC32 = { operand, _ ->
        val newValue = vm.vmMemory.getInt(operand.operand.value) - 1
        vm.vmMemory.putInt(operand.operand.value, newValue)
        newValue.toLong()
      },
      handleC64 = { _, eip ->
        throw VmExecutionException(eip, "Cannot dec C64")
      },
      handleC32 = { _, eip ->
        throw VmExecutionException(eip, "Cannot dec C32")
      }
    )

    vm.vmFlags.updateFlagsFromResult(result)
    return currentEip + 1
  }

}