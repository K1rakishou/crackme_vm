package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.handlers.helpers.GenericTwoOperandsInstructionHandler
import crackme.vm.instructions.Xor

class XorHandler : Handler<Xor>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Xor): Int {
    //FIXME
//    GenericTwoOperandsInstructionHandler.handle(
//      vm,
//      currentEip,
//      instruction,
//      handle_Reg_C64 = { dest, src, _ ->
//        vm.registers[dest.index] = vm.registers[dest.index] xor src.value
//      },
//      handle_Reg_C32 = { dest, src, _ ->
//        vm.registers[dest.index] = vm.registers[dest.index] xor src.value.toLong()
//      },
//      handle_Reg_MemC64 = { dest, src, _ ->
//        vm.registers[dest.index] = vm.registers[dest.index] xor getConstantValueFromVmMemory(vm, src.operand)
//      },
//      handle_Reg_MemC32 = { dest, src, _ ->
//        vm.registers[dest.index] = vm.registers[dest.index] xor getConstantValueFromVmMemory(vm, src.operand)
//      },
//      handle_Reg_MemReg = { dest, src, eip ->
//        vm.registers[dest.index] = vm.registers[dest.index] xor getVmMemoryValueByRegister(vm, eip, src)
//      },
//      handle_Reg_MemVar = { dest, src, eip ->
//        vm.registers[dest.index] = vm.registers[dest.index] xor getVmMemoryValueByVariable(vm, eip, src)
//      },
//      handle_Reg_Reg = { dest, src, _ ->
//        vm.registers[dest.index] = vm.registers[dest.index] xor vm.registers[src.index]
//      },
//      handle_Reg_Var = { dest, src, _ ->
//        vm.registers[dest.index] = vm.registers[dest.index] xor src.address.toLong()
//      },
//      handle_MemReg_Reg = { dest, src, eip ->
//        val newValue = getVmMemoryValueByRegister(vm, eip, dest) xor vm.registers[src.index]
//        putVmMemoryValueByRegister(dest, vm, newValue, eip)
//      },
//      handle_MemVar_Reg = { dest, src, eip ->
//        val newValue = getVmMemoryValueByVariable(vm, eip, dest) xor vm.registers[src.index]
//        putVmMemoryValueByVariable(dest, vm, newValue, eip)
//      },
//      handle_MemC64_Reg = { dest, src, _ ->
//        val address = dest.operand.value.toInt()
//        val newValue = vm.vmMemory.getLong(address) xor vm.registers[src.index]
//        vm.vmMemory.putLong(address, newValue)
//      },
//      handle_MemC32_Reg = { dest, src, _ ->
//        val address = dest.operand.value
//        val newValue = vm.vmMemory.getInt(address) xor vm.registers[src.index].toInt()
//        vm.vmMemory.putInt(address, newValue)
//      },
//      handle_MemReg_Const = { dest, src, eip ->
//        val constantValue = getConstantValueFromVmMemory(vm, src)
//        val newValue = getVmMemoryValueByRegister(vm, eip, dest) xor constantValue
//        putVmMemoryValueByRegister(dest, vm, newValue, eip)
//      },
//      handle_MemVar_Const = { dest, src, eip ->
//        val constantValue = getConstantValueFromVmMemory(vm, src)
//        val newValue = getVmMemoryValueByVariable(vm, eip, dest) xor constantValue
//        putVmMemoryValueByVariable(dest, vm, newValue, eip)
//      }
//    )

    // TODO: flags
    // vm.vmFlags.updateFlagsFromResult(result)

    return currentEip + 1
  }

}