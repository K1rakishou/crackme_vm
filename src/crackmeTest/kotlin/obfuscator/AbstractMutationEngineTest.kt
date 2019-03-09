package sample.helloworld.obfuscator

import crackme.vm.VM
import crackme.vm.VMSimulator
import crackme.vm.instructions.Instruction
import crackme.vm.obfuscator.mutation.MutationEngine
import crackme.vm.obfuscator.mutation.SimpleMutationEngine
import kotlin.random.Random

abstract class AbstractMutationEngineTest {
  private val random = Random(0)

  fun <T : Instruction> test(count: Int = 100, mutator: (MutationEngine) -> List<Instruction>, checker: (VM) -> Unit) {
    val mutationEngine = SimpleMutationEngine(random)

    for (i in 0 until count) {
      val mutatedCode = mutator(mutationEngine)
//
//      for (instruction in mutatedCode) {
//        println("[Attempt $i]: $instruction")
//      }

      val vm = VM(random, mutatedCode)
      val vmSimulator = VMSimulator(true)
      vmSimulator.simulate(vm)

      checker(vm)
    }
  }

}