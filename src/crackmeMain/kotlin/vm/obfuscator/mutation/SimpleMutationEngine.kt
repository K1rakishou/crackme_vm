package crackme.vm.obfuscator.mutation

import crackme.vm.instructions.Instruction
import crackme.vm.instructions.Mov
import crackme.vm.instructions.Sub
import crackme.vm.operands.*
import kotlin.math.abs
import kotlin.random.Random

class SimpleMutationEngine(
  private val random: Random
) : MutationEngine {

  /**
   * mov r0, 500
   *
   *      |
   *      v
   *
   * mov r0, 250
   * add r0, 250
   *
   *      OR
   *
   * mov r0, 1000
   * sub r0, 500
   *
   *      OR
   *
   * mov r0, 11223236
   * xor r0, 11223344
   * */


  private val movSubstituteList: List<(Register, Constant) -> List<Instruction>> = listOf(
    { register, constant ->
      when (constant) {
        is C32 -> {
          val originalValue = constant.value
          val randomValue = abs(random.nextInt())
          val fakeValue = randomValue - originalValue

          return@listOf listOf(
            Mov(register, C32(randomValue)),
            Sub(register, C32(fakeValue)) as Instruction
          )
        }
        is C64 -> {
          val originalValue = constant.value
          val randomValue = abs(random.nextLong())
          val fakeValue = randomValue - originalValue

          return@listOf listOf(
            Mov(register, C64(randomValue)),
            Sub(register, C64(fakeValue)) as Instruction
          )
        }
        else -> throw RuntimeException("Not implemented for $constant")
      }
    }
  )

  override fun mutateMov(instruction: Mov): List<Instruction> {
    return movSubstituteList[0].invoke(instruction.dest as Register, instruction.src as Constant)
  }
}