package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.instructions.Ret

class RetHandler : Handler<Ret>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Ret): Int {
    if (vm.vmStack.isEmpty()) {
      return Int.MAX_VALUE
    }

    val newEip = vm.vmStack.pop<Int>(AddressingMode.ModeDword).toInt()
    vm.vmStack.cleatTop(instruction.bytesToClearFromStack)

    //return from the current function
    return newEip
  }

}