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
      handleMemReg = { operand, eip ->
        val newValue = getVmMemoryValueByRegister(vm, eip, operand) - 1
        putVmMemoryValueByRegister(operand, vm, newValue, eip)
        newValue
      },
      handleMemConstant = { operand, eip ->
        val newValue = getVmMemoryValueByConstant(vm, eip, operand) - 1
        putVmMemoryValueByConstant(operand, vm, newValue, eip)
        newValue
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