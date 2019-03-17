package crackme.vm.handlers

import crackme.vm.VM
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
      handle_Reg_Constant = { dest, src, eip -> vm.registers[dest.index] = extractValueFromConstant(eip, src) },
      handle_Reg_MemC32 = { dest, src, eip -> vm.registers[dest.index] = getVmMemoryValueByConstant(vm, eip, src as Memory<Constant>) },
      handle_Reg_MemReg = { dest, src, eip -> vm.registers[dest.index] = getVmMemoryValueByRegister(vm, eip, src) },
      handle_Reg_MemVar = { dest, src, eip -> vm.registers[dest.index] = getVmMemoryValueByVariable(vm, eip, src) },
      handle_Reg_Reg = { dest, src, _ -> vm.registers[dest.index] = vm.registers[src.index] },
      handle_Reg_Var = { dest, src, _ -> vm.registers[dest.index] = src.address.toLong() },
      handle_MemReg_Reg = { dest, src, eip -> putVmMemoryValueByRegister(dest, vm, vm.registers[src.index], eip) },
      handle_MemVar_Reg = { dest, src, eip -> putVmMemoryValueByVariable(dest, vm, vm.registers[src.index], eip) },
      handle_MemC32_Reg = { dest, src, eip -> putVmMemoryValueByConstant(dest as Memory<Constant>, vm, vm.registers[src.index], eip) },
      handle_MemReg_Const = { dest, src, eip -> putVmMemoryValueByRegister(dest, vm, getConstantValueFromVmMemory(vm, src), eip) },
      handle_MemVar_Const = { dest, src, eip -> putVmMemoryValueByVariable(dest, vm, getConstantValueFromVmMemory(vm, src), eip) },
      handle_MemC32_Const = { dest, src, eip -> putVmMemoryValueByConstant(dest as Memory<Constant>, vm, extractValueFromConstant(eip, src), eip) }
    )

    return currentEip + 1
  }

}