package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.Add
import crackme.vm.operands.C32
import crackme.vm.operands.C64
import crackme.vm.operands.Constant
import crackme.vm.operands.Register

class AddHandler : Handler<Add>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Add): Int {
    if (instruction.dest !is Register) {
      throw NotImplementedError("Dest operand must be a register!")
    }

    val dest = instruction.dest

    when (val src = instruction.src) {
      is Constant -> {
        when (src) {
          is C64 -> {
            vm.registers[dest.index] += src.value
            vm.registers[dest.index]
          }
          is C32 -> {
            vm.registers[dest.index] += src.value.toLong()
            vm.registers[dest.index]
          }
          else -> throw VmExecutionException(currentEip, "Add handler not implemented to work with (${src.operandName}) as src operand")
        }
      }
      is Register -> {
        vm.registers[dest.index] += vm.registers[src.index]
        vm.registers[dest.index]
      }
      else -> throw VmExecutionException(currentEip, "Add handler not implemented to work with (${src.operandName}) as src operand")
    }

    return currentEip + 1
  }
}