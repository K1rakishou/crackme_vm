package crackme.vm.obfuscator.generator

import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Mov

interface VMInstructionGenerator {
  fun generateEncryptionSubRoutineForMovConstant(instruction: Mov): List<Instruction>
}