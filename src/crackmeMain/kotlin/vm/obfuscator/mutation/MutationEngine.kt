package crackme.vm.obfuscator.mutation

import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Mov

interface MutationEngine {
  fun mutateMov(instruction: Mov): List<Instruction>
}