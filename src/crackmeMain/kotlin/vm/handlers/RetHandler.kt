package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.core.AddressingMode
import crackme.vm.instructions.Ret

class RetHandler : Handler<Ret>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Ret): Int {
    if (vm.vmStack.isEmpty() || instruction.isVmExit) {
      if (instruction.isVmExit) {
        vm.vmStack.deallocate(instruction.amountToDeallocate)
      }

      //return from the VM
      return Int.MAX_VALUE
    }

    //return from the current function
    val newEip = vm.vmStack.pop<Int>(AddressingMode.ModeDword).toInt()
    vm.vmStack.deallocate(instruction.amountToDeallocate)

    return newEip
  }

}