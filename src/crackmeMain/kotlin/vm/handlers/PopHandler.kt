package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Pop
import crackme.vm.operands.C32
import crackme.vm.operands.C64
import crackme.vm.operands.Memory
import crackme.vm.operands.Register

class PopHandler : Handler<Pop>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Pop): Int {
    val result = GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ ->
        when (instruction.addressingMode) {
          AddressingMode.ModeByte -> {
            val value = vm.vmStack.pop<Byte>(instruction.addressingMode).toLong()
            vm.registers[operand.index] = value
            value
          }
          AddressingMode.ModeWord -> {
            val value = vm.vmStack.pop<Short>(instruction.addressingMode).toLong()
            vm.registers[operand.index] = value
            value
          }
          AddressingMode.ModeDword -> {
            val value = vm.vmStack.pop<Int>(instruction.addressingMode).toLong()
            vm.registers[operand.index] = value
            value
          }
          AddressingMode.ModeQword -> {
            val value = vm.vmStack.pop<Long>(instruction.addressingMode).toLong()
            vm.registers[operand.index] = value
            value
          }
        }
      },
      handleMemReg = { operand, eip ->
        popIntoVmMemoryByRegister(operand, instruction.addressingMode, vm, eip)
      },
      handleMemConstant = { operand, eip ->
        val value = when (operand.operand) {
          is C64 -> vm.vmStack.pop<Long>(instruction.addressingMode)
          is C32 -> vm.vmStack.pop<Int>(instruction.addressingMode).toLong()
          else -> throw VmExecutionException(eip, "Cannot pop into VmString")
        }

        //TODO: may not work properly
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

  private fun popIntoVmMemoryByRegister(operand: Memory<Register>, addressingMode: AddressingMode, vm: VM, eip: Int): Long {
    return when (operand.addressingMode) {
      AddressingMode.ModeByte -> {
        val value = vm.vmStack.pop<Byte>(addressingMode)
        putVmMemoryValueByRegister(operand, vm, value.toLong(), eip)
        value.toLong()
      }
      AddressingMode.ModeWord -> {
        val value = vm.vmStack.pop<Short>(addressingMode)
        putVmMemoryValueByRegister(operand, vm, value.toLong(), eip)
        value.toLong()
      }
      AddressingMode.ModeDword -> {
        val value = vm.vmStack.pop<Int>(addressingMode)
        putVmMemoryValueByRegister(operand, vm, value.toLong(), eip)
        value.toLong()
      }
      AddressingMode.ModeQword -> {
        val value = vm.vmStack.pop<Long>(addressingMode)
        putVmMemoryValueByRegister(operand, vm, value, eip)
        value
      }
    }
  }

}