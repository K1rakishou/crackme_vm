package crackme.vm

import crackme.vm.core.VmFlags
import crackme.vm.core.function.NativeFunctionType
import crackme.vm.core.VmMemory
import crackme.vm.core.VmStack
import crackme.vm.instructions.Instruction
import crackme.vm.core.function.NativeFunction
import kotlin.random.Random

class VM(
  random: Random = Random(0),
  val instructions: List<Instruction>,
  val nativeFunctions: Map<NativeFunctionType, NativeFunction> = mapOf(),
  val registers: MutableList<Long> = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0),
  val labels: MutableMap<String, Int> = mutableMapOf(),
  val vmStack: VmStack = VmStack(),
  val vmMemory: VmMemory = VmMemory(1024, random),
  val vmFlags: VmFlags = VmFlags()
)