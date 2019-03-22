package crackme.vm.parser

import crackme.vm.core.VmFunctionScope
import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Let

class LetTransformer {

  fun transform(vmFunctionScope: VmFunctionScope, instruction: Let): List<Instruction> {
    return listOf(instruction)
  }

}