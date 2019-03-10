package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Inc

class IncHandler : Handler<Inc>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Inc): Int {
    GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ -> ++vm.registers[operand.index] },
      handleMemReg = { operand, _ ->
        val address = vm.registers[operand.operand.index].toInt()
        val oldValue = vm.vmMemory.getLong(address)
        vm.vmMemory.putLong(address, oldValue + 1)
      },
      handleMemVar = { operand, eip ->
        val oldValue = getVmMemoryValueByVariable(vm, eip, operand)
        putVmMemoryValueByVariable(operand, vm, oldValue + 1, eip)
      },
      handleMemC64 = { operand, _ ->
        val oldValue = vm.vmMemory.getLong(operand.operand.value.toInt())
        vm.vmMemory.putLong(operand.operand.value.toInt(), oldValue + 1)
      },
      handleMemC32 = { operand, _ ->
        val oldValue = vm.vmMemory.getInt(operand.operand.value)
        vm.vmMemory.putInt(operand.operand.value, oldValue + 1)
      },
      handleC64 = { _, eip ->
        throw VmExecutionException(eip, "Cannot inc C64")
      },
      handleC32 = { _, eip ->
        throw VmExecutionException(eip, "Cannot inc C32")
      }
    )

    //TODO: update flags
    return currentEip + 1
  }

}