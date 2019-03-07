package crackme.vm

import crackme.vm.core.os.File

class VMCompiler {

  fun compile(file: File, vm: VM) {
    val REGISTERS_COUNT = vm.registers.size
    val REGISTERS_OFFSET = 0
    val SIZE_OF_REGISTER = 8

    for ((index, registerValue) in vm.registers.withIndex()) {
      println("r[$index] = $registerValue")
    }

    for (instruction in vm.instructions) {
      println("${instruction} (rawSize = ${instruction.getInstructionRawSize()})")
    }
  }

}