package crackme.vm

import crackme.vm.core.VmFlags
import crackme.vm.core.VmFunction
import crackme.vm.core.VmMemory
import crackme.vm.core.VmStack
import crackme.vm.core.function.NativeFunction
import crackme.vm.core.function.NativeFunctionType
import kotlin.random.Random

class VM private constructor(
  val vmFunctions: MutableMap<String, VmFunction>,
  val nativeFunctions: Map<NativeFunctionType, NativeFunction>,
  val vmMemory: VmMemory,
  val registers: MutableList<Long>
) {
  private val random: Random = Random(0)

  val vmStack: VmStack = VmStack(1024, registers, random)
  val vmFlags: VmFlags = VmFlags()

  companion object {
    const val spRegOffset = 8
    const val ipRegOffset = 9
    const val mainFunctionName = "main"

    fun createVM(
      vmFunctions: MutableMap<String, VmFunction>,
      nativeFunctions: Map<NativeFunctionType, NativeFunction>,
      vmMemory: VmMemory,
      registers: MutableList<Long>
    ): VM {
      return VM(
        vmFunctions,
        nativeFunctions,
        vmMemory,
        registers
      )
    }
  }
}