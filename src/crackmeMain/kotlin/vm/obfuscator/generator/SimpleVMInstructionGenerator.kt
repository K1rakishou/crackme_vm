package crackme.vm.obfuscator.generator

import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Mov
import crackme.vm.obfuscator.mutation.MutationEngine
import kotlin.random.Random

class SimpleVMInstructionGenerator(
  private val random: Random,
  private val mutationEngine: MutationEngine
) : VMInstructionGenerator {

  override fun generateEncryptionSubRoutineForMovConstant(instruction: Mov): List<Instruction> {
    return mutationEngine.mutateMov(instruction)
  }

}