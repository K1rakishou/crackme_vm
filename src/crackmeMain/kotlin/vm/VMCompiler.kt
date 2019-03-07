package crackme.vm

import crackme.vm.core.Utils
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
      val compiled = instruction.compile().map { Utils.bytesToHex(it) }.joinToString()
      println("${instruction} (rawSize = ${instruction.getInstructionRawSize()}) (byte code = [${compiled}])")
    }

    println("result = ${vm.registers[0]}")
  }

}