package crackme.vm.instructions

import crackme.vm.VM
import crackme.vm.core.VmExecutionException
import crackme.vm.handlers.Handler
import crackme.vm.operands.C32
import crackme.vm.operands.C64
import crackme.vm.operands.Constant
import crackme.vm.operands.Register

class CmpHandler : Handler<Cmp>() {

  override fun handle(vm: VM, currentEip: Int, instruction: Cmp): Int {
    if (instruction.dest !is Register && instruction.src !is Register) {
      throw VmExecutionException(currentEip, "Only the Register operand type is supported for now")
    }

    val dest = instruction.dest as Register

    val result = when (val src = instruction.src) {
      is Constant -> {
        when (src) {
          is C64 -> src.value - vm.registers[dest.index]
          is C32 -> src.value.toLong() - vm.registers[dest.index]
          else -> throw VmExecutionException(currentEip, "Cmp handler not implemented to work with (${src.operandName}) as src operand")
        }
      }
      is Register -> vm.registers[src.index] - vm.registers[dest.index]
      else -> throw VmExecutionException(currentEip, "Cmp handler not implemented to work with (${src.operandName}) as src operand")
    }

    vm.vmFlags.updateFlagsFromResult(result)
    return currentEip + 1
  }

}