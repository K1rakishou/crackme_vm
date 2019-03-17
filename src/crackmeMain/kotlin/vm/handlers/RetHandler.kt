package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.instructions.Ret

class RetHandler : Handler<Ret>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Ret): Int {
    if (vm.vmStack.isEmpty()) {
      return Int.MAX_VALUE
    }

    val bytesToPop = instruction.bytesToClearFromStack
    if (bytesToPop % 4 != 0) {
      throw VmExecutionException(currentEip, "Ret operand must be divisible by INT_SIZE ($bytesToPop)")
    }

    val popsCount = bytesToPop / 4

    //clear stack
    for (i in 0 until popsCount) {
      //TODO: implement function to do this in one operation
      vm.vmStack.pop32()
    }

    //return from the current function
    return vm.vmStack.pop64().toInt()
  }

}