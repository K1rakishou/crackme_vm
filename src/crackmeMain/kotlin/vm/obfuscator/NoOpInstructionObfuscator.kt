package crackme.vm.obfuscator

import crackme.vm.core.VmMemory
import crackme.vm.instructions.Instruction

class NoOpInstructionObfuscator : VMInstructionObfuscator {
  override fun obfuscate(vmMemory: VmMemory, instruction: Instruction): List<Instruction> {
    return listOf(instruction)
  }
}