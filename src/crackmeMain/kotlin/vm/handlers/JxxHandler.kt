package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.JumpType
import crackme.vm.core.VmExecutionException
import crackme.vm.core.VmFlags
import crackme.vm.instructions.Jxx

class JxxHandler : Handler<Jxx>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Jxx): Int {
    when (instruction.jumpType) {
      JumpType.Je -> {
        if (vm.vmFlags.isFlagSet(VmFlags.Flag.ZF)) {
          return getNewEip(vm, currentEip, instruction)
        }
      }
      JumpType.Jne -> {
        if (!vm.vmFlags.isFlagSet(VmFlags.Flag.ZF)) {
          return getNewEip(vm, currentEip, instruction)
        }
      }
      JumpType.Jmp -> return getNewEip(vm, currentEip, instruction)
    }

    return currentEip + 1
  }

  private fun getNewEip(vm: VM, eip: Int, instruction: Jxx): Int {
    val vmFunction = vm.vmFunctions[instruction.functionName]
    if (vmFunction == null) {
      throw VmExecutionException(eip, "Cannot find vmFunction with name ${instruction.functionName}")
    }

    val newEip = vmFunction.labels[instruction.labelName]
    if (newEip == null) {
      throw VmExecutionException(eip, "Cannot find label with name ${instruction.labelName}")
    }

    return newEip
  }
}