package crackme.vm.core

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class VmFlags(
  private var flags: Byte = 0
) {
  // [7, 6, 5, 4, 3, 2, 1, 0]
  //  ^
  // ZF

  fun setFlag(flag: Flag) {
    flags = flags or flag.value
  }

  fun resetFlag(flag: Flag) {
    flags = flags and flag.value.inv()
  }

  fun isFlagSet(flag: Flag): Boolean {
    return flags and flag.value != 0.toByte()
  }

  fun getValue(): Byte = flags

  enum class Flag(val value: Byte) {
    ZF((1 shl 7).toByte())
  }
}