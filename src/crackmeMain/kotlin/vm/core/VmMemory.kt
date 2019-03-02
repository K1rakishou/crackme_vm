package crackme.vm.core

import kotlin.random.Random

class VmMemory(private val size: Int,
               private val random: Random) {
  private val memory = random.nextBytes(size)

  fun putInt(index: Int, value: Int) {
    memory[index] = ((value shr 24) and 0x000000FF).toByte()
    memory[index + 1] = ((value shr 16) and 0x000000FF).toByte()
    memory[index + 2] = ((value shr 8) and 0x000000FF).toByte()
    memory[index + 3] = (value and 0x000000FF).toByte()
  }

  fun getInt(index: Int): Int {
    var result = 0

    for (i in 0..3) {
      result = result shl 8
      result = result or (memory[index + i].toInt() and 0xFF)
    }

    return result
  }
}