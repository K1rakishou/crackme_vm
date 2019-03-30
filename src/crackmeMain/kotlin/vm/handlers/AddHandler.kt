package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.handlers.helpers.GenericTwoOperandsInstructionHandler
import crackme.vm.instructions.Add
import crackme.vm.operands.Constant
import crackme.vm.operands.Memory

class AddHandler : Handler<Add>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Add): Int {
    val result = GenericTwoOperandsInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handle_Reg_Constant = { dest, src, eip ->
        vm.registers[dest.index] = vm.registers[dest.index] + extractValueFromConstant(eip, src, AddressingMode.ModeQword)
        vm.registers[dest.index]
      },
      handle_Reg_MemC32 = { dest, src, eip ->
        vm.registers[dest.index] = vm.registers[dest.index] + getVmMemoryValueByConstant(vm, eip, src as Memory<Constant>)
        vm.registers[dest.index]
      },
      handle_Reg_MemReg = { dest, src, eip ->
        vm.registers[dest.index] = vm.registers[dest.index] + getVmMemoryValueByRegister(vm, eip, src)
        vm.registers[dest.index]
      },
      handle_Reg_Reg = { dest, src, _ ->
        vm.registers[dest.index] = vm.registers[dest.index] + vm.registers[src.index]
        vm.registers[dest.index]
      },
      handle_MemReg_Reg = { dest, src, eip ->
        val newValue = getVmMemoryValueByRegister(vm, eip, dest) + vm.registers[src.index]
        putVmMemoryValueByRegister(dest, vm, newValue, eip)
        newValue
      },
      handle_MemC32_Reg = { dest, src, eip ->
        val newValue = getVmMemoryValueByConstant(vm, eip, dest as Memory<Constant>) + vm.registers[src.index]
        putVmMemoryValueByConstant(dest as Memory<Constant>, vm, newValue, eip)
        newValue
      },
      handle_MemReg_Const = { dest, src, eip ->
        val constantValue = getConstantValueFromVmMemory(vm, src)
        val newValue = getVmMemoryValueByRegister(vm, eip, dest) + constantValue
        putVmMemoryValueByRegister(dest, vm, newValue, eip)
        newValue
      },
      handle_MemC32_Const = { dest, src, eip ->
        val constantValue = extractValueFromConstant(eip, src, dest.addressingMode)
        val newValue = getVmMemoryValueByConstant(vm, eip, dest as Memory<Constant>) + constantValue
        putVmMemoryValueByConstant(dest, vm, newValue, eip)
        newValue
      }
    )

    vm.vmFlags.updateFlagsFromResult(result)
    return currentEip + 1
  }
}