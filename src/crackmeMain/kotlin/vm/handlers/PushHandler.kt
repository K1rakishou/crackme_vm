package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.helpers.GenericOneOperandInstructionHandler
import crackme.vm.instructions.Push
import crackme.vm.operands.*

class PushHandler : Handler<Push>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Push): Int {
    GenericOneOperandInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handleReg = { operand, _ ->
        vm.vmStack.push64(vm.registers[operand.index])
        0
      },
      handleMemReg = { operand, eip ->
        pushByMemoryRegister(vm, eip, operand)
      },
      handleMemVar = { operand, eip ->
        pushByMemoryVariable(vm, eip, operand)
      },
      handleMemConstant = { operand, eip ->
        //TODO: may work incorrectly, needs additional testing
        when (operand.operand) {
          is C32 -> vm.vmStack.push64(getConstantValueFromVmMemory(vm, operand.operand))
          //TODO: probably we don't even need C64 here since VmMemory size is a 32 bit value and will never be 64 bit value
          is C64 -> vm.vmStack.push64(getConstantValueFromVmMemory(vm, operand.operand))
          else -> throw VmExecutionException(eip, "Cannot push VmString to the stack")
        }

        0
      },
      handleC64 = { operand, eip ->
        println("pushing64 ${operand.value}")
        vm.vmStack.push64(operand.value)
        0
      },
      handleC32 = { operand, eip ->
        //TODO addressing mode for push/pop
        //push64 here because C64 is the default constant size

        println("pushing32 ${operand.value.toLong()}")
        vm.vmStack.push64(operand.value.toLong())
        0
      }
    )

    //we do need to update flags here because this instruction does not change registers
    return currentEip + 1
  }

  private fun pushByMemoryVariable(vm: VM, eip: Int, operand: Memory<Variable>): Long {
    val value = getVmMemoryValueByVariable(vm, eip, operand)

    return when (operand.addressingMode) {
      AddressingMode.ModeByte -> TODO("push8  not implemented yet")
      AddressingMode.ModeWord -> TODO("push16 not implemented yet")
      AddressingMode.ModeDword -> {
        vm.vmStack.push32(value.toInt())
        0
      }
      AddressingMode.ModeQword -> {
        vm.vmStack.push64(value)
        0
      }
    }
  }

  private fun pushByMemoryRegister(vm: VM, eip: Int, operand: Memory<Register>): Long {
    val value = getVmMemoryValueByRegister(vm, eip, operand)

    return when (operand.addressingMode) {
      AddressingMode.ModeByte -> TODO("push8  not implemented yet")
      AddressingMode.ModeWord -> TODO("push16 not implemented yet")
      AddressingMode.ModeDword -> {
        vm.vmStack.push32(value.toInt())
        0
      }
      AddressingMode.ModeQword -> {
        vm.vmStack.push64(value)
        0
      }
    }
  }

}