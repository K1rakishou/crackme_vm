package crackme.vm.handlers

import crackme.vm.VM
import crackme.vm.instructions.Ret

class RetHandler : Handler<Ret>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Ret): Int {
    if (vm.vmStack.isEmpty()) {
      return Int.MAX_VALUE
    }

    val newEip = vm.vmStack.pop64().toInt()
    vm.vmStack.cleatTop(instruction.bytesToClearFromStack)

    println("    r0 = ${vm.registers[0]}")
    println("    r1 = ${vm.registers[1]}")
    println("    r2 = ${vm.registers[2]}")
    println("    r3 = ${vm.registers[3]}")
    println("    r4 = ${vm.registers[4]}")
    println("    r5 = ${vm.registers[5]}")

    //return from the current function
    return newEip
  }

}