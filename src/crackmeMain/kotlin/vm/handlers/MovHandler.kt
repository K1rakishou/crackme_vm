package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.handlers.helpers.GenericTwoOperandsInstructionHandler
import crackme.vm.instructions.Mov

class MovHandler : Handler<Mov>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Mov): Int {
    GenericTwoOperandsInstructionHandler.handle(
      vm,
      currentEip,
      instruction,
      handle_Reg_C64 = { dest, src, _ -> vm.registers[dest.index] = src.value },
      handle_Reg_C32 = { dest, src, _ -> vm.registers[dest.index] = src.value.toLong() },
      handle_Reg_MemC64 = { dest, src, _ -> vm.registers[dest.index] = getConstantValue(vm, src.operand) },
      handle_Reg_MemC32 = { dest, src, _ -> vm.registers[dest.index] = getConstantValue(vm, src.operand) },
      handle_Reg_MemReg = { dest, src, _ -> vm.registers[dest.index] = vm.vmMemory.getLong(vm.registers[src.operand.index].toInt()) },
      handle_Reg_MemVar = { dest, src, eip -> vm.registers[dest.index] = getVmMemoryVariableValue(vm, eip, src.operand) },
      handle_Reg_Reg = { dest, src, _ -> vm.registers[dest.index] = vm.registers[src.index] },
      handle_Reg_Var = { dest, src, _ -> vm.registers[dest.index] = src.address.toLong() },
      handle_MemReg_Reg = { dest, src, _ -> vm.vmMemory.putLong(vm.registers[dest.operand.index].toInt(), vm.registers[src.index]) },
      handle_MemVar_Reg = { dest, src, eip -> putVmMemoryVariableValueFromRegister(dest, vm, src, eip) },
      handle_MemC64_Reg = { dest, src, _ -> vm.vmMemory.putLong(dest.operand.value.toInt(), vm.registers[src.index]) },
      handle_MemC32_Reg = { dest, src, _ -> vm.vmMemory.putInt(dest.operand.value, vm.registers[src.index].toInt()) }
    )

    return currentEip + 1
  }

}