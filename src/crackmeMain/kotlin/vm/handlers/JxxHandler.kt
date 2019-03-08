package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.JumpType
import crackme.vm.core.VmFlags
import crackme.vm.instructions.Jxx

class JxxHandler : Handler<Jxx>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Jxx): Int {
    when (instruction.jumpType) {
      JumpType.Je -> {
        if (vm.vmFlags.isFlagSet(VmFlags.Flag.ZF)) {
          return instruction.instructionIndex
        }
      }
      JumpType.Jne -> {
        if (!vm.vmFlags.isFlagSet(VmFlags.Flag.ZF)) {
          return instruction.instructionIndex
        }
      }
      JumpType.Jmp -> return instruction.instructionIndex
    }

    return currentEip + 1
  }

}