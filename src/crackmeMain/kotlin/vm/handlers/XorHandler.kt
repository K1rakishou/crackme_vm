package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.handlers.helpers.GenericTwoOperandsInstructionHandler
import crackme.vm.instructions.Xor
import crackme.vm.operands.Constant
import crackme.vm.operands.Memory

class XorHandler : Handler<Xor>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Xor): Int {
    val result = GenericTwoOperandsInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handle_Reg_Constant = { dest, src, eip ->
        vm.registers[dest.index] = vm.registers[dest.index] xor extractValueFromConstant(eip, src, AddressingMode.ModeQword)
        vm.registers[dest.index]
      },
      handle_Reg_MemC32 = { dest, src, _ ->
        vm.registers[dest.index] = vm.registers[dest.index] xor getConstantValueFromVmMemory(vm, src.operand)
        vm.registers[dest.index]
      },
      handle_Reg_MemReg = { dest, src, eip ->
        vm.registers[dest.index] = vm.registers[dest.index] xor getVmMemoryValueByRegister(vm, eip, src)
        vm.registers[dest.index]
      },
      handle_Reg_MemVar = { dest, src, eip ->
        TODO("remove me handle_Reg_MemVar")
//        vm.registers[dest.index] = vm.registers[dest.index] xor getVmMemoryValueByVariable(vm, eip, src)
//        vm.registers[dest.index]
      },
      handle_Reg_Reg = { dest, src, _ ->
        vm.registers[dest.index] = vm.registers[dest.index] xor vm.registers[src.index]
        vm.registers[dest.index]
      },
      handle_Reg_Var = { dest, src, _ ->
        TODO("XorRegVar is forbidden")
//        vm.registers[dest.index] = vm.registers[dest.index] xor src.stackFrame.toLong()
//        vm.registers[dest.index]
      },
      handle_MemReg_Reg = { dest, src, eip ->
        val newValue = getVmMemoryValueByRegister(vm, eip, dest) xor vm.registers[src.index]
        putVmMemoryValueByRegister(dest, vm, newValue, eip)
        newValue
      },
      handle_MemVar_Reg = { dest, src, eip ->
        TODO("remove me handle_MemVar_Reg")

//        val newValue = getVmMemoryValueByVariable(vm, eip, dest) xor vm.registers[src.index]
//        putVmMemoryValueByVariable(dest, vm, newValue, eip)
//        newValue
      },
      handle_MemC32_Reg = { dest, src, _ ->
        val address = dest.operand.value
        val newValue = vm.vmMemory.getInt(address) xor vm.registers[src.index].toInt()
        vm.vmMemory.putInt(address, newValue)
        newValue.toLong()
      },
      handle_MemReg_Const = { dest, src, eip ->
        val constantValue = getConstantValueFromVmMemory(vm, src)
        val newValue = getVmMemoryValueByRegister(vm, eip, dest) xor constantValue
        putVmMemoryValueByRegister(dest, vm, newValue, eip)
        newValue
      },
      handle_MemVar_Const = { dest, src, eip ->
        TODO("remove me handle_MemVar_Const")

//        val constantValue = getConstantValueFromVmMemory(vm, src)
//        val newValue = getVmMemoryValueByVariable(vm, eip, dest) xor constantValue
//        putVmMemoryValueByVariable(dest, vm, newValue, eip)
//        newValue
      },
      handle_MemC32_Const = { dest, src, eip ->
        val constantValue = extractValueFromConstant(eip, src, dest.addressingMode)
        val newValue = getVmMemoryValueByConstant(vm, eip, dest as Memory<Constant>) xor constantValue
        putVmMemoryValueByConstant(dest, vm, newValue, eip)
        newValue
      }
    )

    vm.vmFlags.updateFlagsFromResult(result)
    return currentEip + 1
  }

}