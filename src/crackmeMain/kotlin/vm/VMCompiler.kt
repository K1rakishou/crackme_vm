package crackme.vm

import crackme.vm.core.Utils
import crackme.vm.core.os.File

class VMCompiler {

  fun compile(file: File, vm: VM) {
    val REGISTERS_COUNT = vm.registers.size
    val REGISTERS_OFFSET = 0
    val SIZE_OF_REGISTER = 8

    for (instruction in vm.instructions) {
      val compiled = instruction.compile()
      val hexStr = compiled.map { Utils.bytesToHex(it) }.joinToString()

      println("${instruction.toString().padEnd(30)} (rawSize = ${instruction.getInstructionRawSize().toString().padEnd(4)}) (byte code = [${hexStr}])")
    }

    println("result = ${vm.registers[0]}")
  }

}