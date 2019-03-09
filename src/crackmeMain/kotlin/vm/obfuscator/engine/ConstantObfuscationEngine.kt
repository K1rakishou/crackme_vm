package crackme.vm.obfuscator.engine

import crackme.vm.core.VmMemory
import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Mov
import crackme.vm.obfuscator.generator.VMInstructionGenerator
import crackme.vm.operands.*

class ConstantObfuscationEngine(
  private val vmInstructionGenerator: VMInstructionGenerator
) {

  fun obfuscateMov(vmMemory: VmMemory, instruction: Mov): List<Instruction> {
    if (instruction.dest !is Register && instruction.src !is Constant) {
      return emptyList()
    }

    return vmInstructionGenerator.generateEncryptionSubRoutineForMovConstant(instruction)
  }

}