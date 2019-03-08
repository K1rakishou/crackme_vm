package crackme.vm.obfuscator

import crackme.vm.instructions.Instruction

class NoOpInstructionObfuscator : VMInstructionObfuscator {
  override fun obfuscate(instruction: Instruction): List<Instruction> {
    return listOf(instruction)
  }
}