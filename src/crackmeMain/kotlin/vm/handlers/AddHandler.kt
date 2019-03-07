package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.VMSimulator
import crackme.vm.instructions.Add
import crackme.vm.operands.C64
import crackme.vm.operands.Constant
import crackme.vm.operands.Register

class AddHandler : Handler<Add> {

  override fun handle(vm: VM, eip: Int, instruction: Add) {
    if (instruction.dest !is Register) {
      throw NotImplementedError("Dest operand must be a register!")
    }

    val dest = instruction.dest

    when (val src = instruction.src) {
      is Constant -> {
        if (src is C64) {
          vm.registers[dest.index] += src.value
        } else {
          throw VMSimulator.VmExecutionException(eip, "Add handler not implemented to work with (${src.operandName}) as src operand")
        }
      }
      is Register -> vm.registers[dest.index] += vm.registers[src.index]
      else -> throw VMSimulator.VmExecutionException(eip, "Add handler not implemented to work with (${src.operandName}) as src operand")
    }
  }
}