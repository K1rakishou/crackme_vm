package crackme.misc

import crackme.vm.VM
import crackme.vm.instructions.Instruction

val Any?.safe get() = Unit

fun extractInstructionsAndGetEntryPoint(vm: VM): Pair<List<Instruction>, Int> {
  var entryPoint = 0

  for (vmFunction in vm.vmFunctions) {
    if (vmFunction.key == "main") {
      break
    }

    entryPoint += vmFunction.value.instructions.size
  }

  val instructions = mutableListOf<Instruction>()
  for (vmFunction in vm.vmFunctions) {
    instructions.addAll(vmFunction.value.instructions.map { it.value })
  }

  return Pair(instructions, entryPoint)
}