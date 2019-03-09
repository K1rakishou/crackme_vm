package crackme.vm.obfuscator

import crackme.vm.core.VmMemory
import crackme.vm.instructions.Instruction

interface VMInstructionObfuscator {
  fun obfuscate(vmMemory: VmMemory, instruction: Instruction): List<Instruction>
}