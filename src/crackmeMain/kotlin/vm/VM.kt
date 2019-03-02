package crackme.vm

import crackme.vm.core.NativeFunctionType
import crackme.vm.instructions.Instruction
import crackme.vm.meta.NativeFunction

class VM(
  val nativeFunctions: Map<NativeFunctionType, NativeFunction>,
  val instructions: List<Instruction>,
  val registers: List<Long>
)