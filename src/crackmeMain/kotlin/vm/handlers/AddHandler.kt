package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.instructions.Add
import crackme.vm.operands.Register

class AddHandler : Handler<Add> {

  override fun handle(vm: VM, eip: Int, instruction: Add) {
    if (instruction.dest !is Register && instruction.src !is Register) {
      throw NotImplementedError("Add handler only supports both operands as Registers for now!")
    }

    val dest = instruction.dest as Register
    val src = instruction.src as Register

    vm.registers[dest.index] += vm.registers[src.index]
  }
}