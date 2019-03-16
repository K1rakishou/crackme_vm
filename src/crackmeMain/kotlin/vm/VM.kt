package crackme.vm

import crackme.vm.core.*
import crackme.vm.core.function.NativeFunctionType
import crackme.vm.core.function.NativeFunction
import kotlin.random.Random

class VM(
  random: Random = Random(0),
  val vmFunctions: MutableMap<String, VmFunction> = mutableMapOf(),
  val nativeFunctions: Map<NativeFunctionType, NativeFunction> = mapOf(),
  val registers: MutableList<Long> = mutableListOf(0, 0, 0, 0, 0, 0, 0, 0),
  val vmStack: VmStack = VmStack(1024, random),
  val vmMemory: VmMemory = VmMemory(1024, random),
  val vmFlags: VmFlags = VmFlags()
)