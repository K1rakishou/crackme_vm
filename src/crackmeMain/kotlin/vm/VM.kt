package crackme.vm

import crackme.vm.core.VmFlags
import crackme.vm.core.function.NativeFunctionType
import crackme.vm.core.VmMemory
import crackme.vm.core.VmStack
import crackme.vm.instructions.Instruction
import crackme.vm.core.function.NativeFunction

class VM(
  val nativeFunctions: Map<NativeFunctionType, NativeFunction>,
  val instructions: List<Instruction>,
  val registers: MutableList<Long>,
  val vmStack: VmStack,
  val vmMemory: VmMemory,
  val vmFlags: VmFlags
)