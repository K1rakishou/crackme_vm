package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.instructions.Ret

class RetHandler : Handler<Ret>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Ret): Int {
    if (vm.vmStack.isEmpty()) {
      return Int.MAX_VALUE
    }

    return vm.vmStack.pop64().toInt()
  }

}