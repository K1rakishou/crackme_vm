package crackme.vm.obfuscator.mutation

import crackme.vm.instructions.*
import crackme.vm.operands.C32
import crackme.vm.operands.C64
import crackme.vm.operands.Constant
import crackme.vm.operands.Register
import kotlin.math.abs
import kotlin.random.Random

class SimpleMutationEngine(
  private val random: Random
) : MutationEngine {

  private val movSubstituteList: List<(Register, Constant) -> List<Instruction>> = listOf(
    /**
     * Mov + Add
     *  |
     *  V
     * mov r0, 500
     *  |
     *  v
     * mov r0, 250
     * add r0, 250
     * */
    { register, constant ->
      when (constant) {
        is C32 -> {
          val originalValue = constant.value
          val randomValue = random.nextInt()
          val fakeValue = randomValue + originalValue

          return@listOf listOf(
            Mov(register, C32(fakeValue)),
            Add(register, C32(-randomValue)) as Instruction
          )
        }
        is C64 -> {
          val originalValue = constant.value
          val randomValue = random.nextLong()
          val fakeValue = randomValue + originalValue

          return@listOf listOf(
            Mov(register, C64(fakeValue)),
            Add(register, C64(-randomValue)) as Instruction
          )
        }
        else -> throw RuntimeException("(Mov + Add) Not implemented for $constant")
      }
    },
    /**
     * Mov + Sub
     *  |
     *  V
     * mov r0, 500
     *  |
     *  v
     * mov r0, 1000
     * sub r0, 500
     * */
    { register, constant ->
      when (constant) {
        is C32 -> {
          val originalValue = constant.value
          val randomValue = random.nextInt()
          val fakeValue = randomValue + originalValue

          return@listOf listOf(
            Mov(register, C32(fakeValue)),
            Sub(register, C32(randomValue)) as Instruction
          )
        }
        is C64 -> {
          val originalValue = constant.value
          val randomValue = random.nextLong()
          val fakeValue = randomValue + originalValue

          return@listOf listOf(
            Mov(register, C64(fakeValue)),
            Sub(register, C64(randomValue)) as Instruction
          )
        }
        else -> throw RuntimeException("(Mov + Sub) Not implemented for $constant")
      }
    },
    /**
     * Mov + Xor
     *  |
     *  V
     * mov r0, 500
     *  |
     *  v
     * mov r0, 1000
     * xor r0, 540
     * */
    { register, constant ->
      when (constant) {
        is C32 -> {
          val originalValue = constant.value
          val randomValue = random.nextInt()
          val fakeValue = randomValue xor originalValue

          return@listOf listOf(
            Mov(register, C32(fakeValue)),
            Xor(register, C32(randomValue)) as Instruction
          )
        }
        is C64 -> {
          val originalValue = constant.value
          val randomValue = random.nextLong()
          val fakeValue = randomValue xor originalValue

          return@listOf listOf(
            Mov(register, C64(fakeValue)),
            Xor(register, C64(randomValue)) as Instruction
          )
        }
        else -> throw RuntimeException("(Mov + Xor) Not implemented for $constant")
      }
    }
  )

  override fun mutateMov(instruction: Mov): List<Instruction> {
    val index = abs(random.nextInt()) % movSubstituteList.size

    return movSubstituteList[index].invoke(
      instruction.dest as Register,
      instruction.src as Constant)
  }
}