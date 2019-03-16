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
          return instruction.instructionId
        }
      }
      JumpType.Jne -> {
        if (!vm.vmFlags.isFlagSet(VmFlags.Flag.ZF)) {
          return instruction.instructionId
        }
      }
      JumpType.Jmp -> return instruction.instructionId
    }

    return currentEip + 1
  }



}