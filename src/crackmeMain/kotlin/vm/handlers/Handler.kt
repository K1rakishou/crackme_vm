package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.instructions.Instruction

interface Handler<T : Instruction> {
  fun handle(vm: VM, eip: Int, instruction: T)
}