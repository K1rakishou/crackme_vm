package crackme.vm.core

import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class VmFlags(
  private var flags: Byte = 0
) {
  // [7, 6, 5, 4, 3, 2, 1, 0]
  //  ^  ^  ^
  // ZF  |  |
  //    GF  |
  //       LF

  fun setFlag(flag: Flag) {
    flags = flags or flag.value
  }

  fun resetFlag(flag: Flag) {
    flags = flags and flag.value.inv()
  }

  fun isFlagSet(flag: Flag): Boolean {
    return flags and flag.value != 0.toByte()
  }

  fun updateFlagsFromResult(result: Long) {
    when {
      result == 0L -> {
        setFlag(VmFlags.Flag.ZF)
        resetFlag(VmFlags.Flag.GF)
        resetFlag(VmFlags.Flag.LF)
      }
      result < 0 -> {
        setFlag(VmFlags.Flag.LF)
        resetFlag(VmFlags.Flag.ZF)
        resetFlag(VmFlags.Flag.GF)
      }
      else -> {
        setFlag(VmFlags.Flag.GF)
        resetFlag(VmFlags.Flag.ZF)
        resetFlag(VmFlags.Flag.LF)
      }
    }
  }

  fun getValue(): Byte = flags

  enum class Flag(val value: Byte) {
    ZF((1 shl 7).toByte()),
    GF((1 shl 6).toByte()),
    LF((1 shl 5).toByte())
  }
}