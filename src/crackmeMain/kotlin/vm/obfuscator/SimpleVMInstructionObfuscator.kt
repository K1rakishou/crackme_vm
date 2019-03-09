package crackme.vm.obfuscator

import crackme.vm.core.VmMemory
import crackme.vm.instructions.Instruction
import crackme.vm.instructions.InstructionType
import crackme.vm.instructions.Mov
import crackme.vm.obfuscator.engine.ConstantObfuscationEngine

class SimpleVMInstructionObfuscator(
  private val constantObfuscationEngine: ConstantObfuscationEngine
) : VMInstructionObfuscator {

  override fun obfuscate(vmMemory: VmMemory, instruction: Instruction): List<Instruction> {
    when (instruction.instructionType) {
      InstructionType.Mov -> return constantObfuscationEngine.obfuscateMov(vmMemory, instruction as Mov)
      else -> {
        //do nothing
      }
    }

    return listOf(instruction)
  }

}