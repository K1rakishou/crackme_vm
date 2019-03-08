package crackme.vm.obfuscator

import crackme.vm.instructions.Instruction

class SimpleVMInstructionObfuscator : VMInstructionObfuscator {

  override fun obfuscate(instruction: Instruction): List<Instruction> {
    //TODO
    return listOf(instruction)
  }

}