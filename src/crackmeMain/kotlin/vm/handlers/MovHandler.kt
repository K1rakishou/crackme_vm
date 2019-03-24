package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.handlers.helpers.GenericTwoOperandsInstructionHandler
import crackme.vm.instructions.Mov
import crackme.vm.operands.Constant
import crackme.vm.operands.Memory

class MovHandler : Handler<Mov>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Mov): Int {
    GenericTwoOperandsInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handle_Reg_Constant = { dest, src, eip ->
        vm.registers[dest.index] = extractValueFromConstant(eip, src, AddressingMode.ModeQword)
        0
      },
      handle_Reg_MemC32 = { dest, src, eip ->
        vm.registers[dest.index] = getVmMemoryValueByConstant(vm, eip, src as Memory<Constant>)
        0
      },
      handle_Reg_MemReg = { dest, src, eip ->
        vm.registers[dest.index] = getVmMemoryValueByRegister(vm, eip, src)
        0
      },
      handle_Reg_Reg = { dest, src, _ ->
        vm.registers[dest.index] = vm.registers[src.index]
        0
      },
      handle_MemReg_Reg = { dest, src, eip ->
        putVmMemoryValueByRegister(dest, vm, vm.registers[src.index], eip)
        0
      },
      handle_MemC32_Reg = { dest, src, eip ->
        putVmMemoryValueByConstant(dest as Memory<Constant>, vm, vm.registers[src.index], eip)
        0
      },
      handle_MemReg_Const = { dest, src, eip ->
        putVmMemoryValueByRegister(dest, vm, getConstantValueFromVmMemory(vm, src), eip)
        0
      },
      handle_MemC32_Const = { dest, src, eip ->
        putVmMemoryValueByConstant(dest as Memory<Constant>, vm, extractValueFromConstant(eip, src, dest.addressingMode), eip)
        0
      }
    )

    //do not update flags in MovHandler
    return currentEip + 1
  }

}