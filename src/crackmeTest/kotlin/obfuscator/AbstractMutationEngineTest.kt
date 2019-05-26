package sample.helloworld.obfuscator

import crackme.vm.VM
import crackme.vm.VMSimulator
import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Ret
import crackme.vm.obfuscator.mutation.MutationEngine
import crackme.vm.obfuscator.mutation.SimpleMutationEngine
import kotlin.random.Random

abstract class AbstractMutationEngineTest {
  protected val random = Random(0)

  fun <T : Instruction> test(count: Int = 100, mutator: (MutationEngine) -> List<Instruction>, checker: (VM) -> Unit) {
    val mutationEngine = SimpleMutationEngine(random)

    for (i in 0 until count) {
      val mutatedCode = mutator(mutationEngine)

      for (instruction in mutatedCode) {
        println("[Attempt $i]: $instruction")
      }

      val mutatedCodeWithReturn = ArrayList(mutatedCode).apply {
        add(Ret(0, true))
      }

      val vm = VM.createTestVM(mutatedCodeWithReturn)
      val vmSimulator = VMSimulator()
      vmSimulator.simulate(vm, 0, mutatedCodeWithReturn)

      checker(vm)
    }
  }

}