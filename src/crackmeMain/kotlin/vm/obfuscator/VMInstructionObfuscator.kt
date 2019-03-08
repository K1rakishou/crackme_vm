package crackme.vm.obfuscator

import crackme.vm.instructions.Instruction

interface VMInstructionObfuscator {
  fun obfuscate(instruction: Instruction): List<Instruction>
}