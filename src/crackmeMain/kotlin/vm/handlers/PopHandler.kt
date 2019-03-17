package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Pop
import crackme.vm.operands.*

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
        popIntoVmMemoryByRegister(operand, vm, eip)
      },
      handleMemVar = { operand, eip ->
        popIntoVmMemoryByVariable(operand, vm, eip)
      },
      handleMemConstant = { operand, eip ->
        val value = when (operand.operand) {
          is C64 -> vm.vmStack.pop64()
          is C32 -> vm.vmStack.pop32().toLong()
          else -> throw VmExecutionException(eip, "Cannot pop into VmString")
        }

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

  private fun popIntoVmMemoryByVariable(operand: Memory<Variable>, vm: VM, eip: Int): Long {
    return when (operand.addressingMode) {
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
  }

  private fun popIntoVmMemoryByRegister(operand: Memory<Register>, vm: VM, eip: Int): Long {
    return when (operand.addressingMode) {
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
  }

}