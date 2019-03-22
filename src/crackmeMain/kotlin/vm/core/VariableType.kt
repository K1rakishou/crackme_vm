package crackme.vm.core

enum class VariableType(val type: Byte,
                        val str: String,
                        val size: Int,
                        val addressingMode: AddressingMode) {
  //TODO add Numeric (or AnyNumber) type to auto convert between C32 and C64 when we don't care about their type
  IntType(0, "Int", 4, AddressingMode.ModeDword),
  LongType(1, "Long", 8, AddressingMode.ModeQword),
  //4 and ModeDword here because we will only use the string address in the VmMemory
  StringType(2, "String", 4, AddressingMode.ModeDword);

  companion object {
    private val map = mapOf(
      "Int" to IntType,
      "Long" to LongType,
      "String" to StringType
    )

    fun fromString(str: String): VariableType? {
      return map[str]
    }
  }
}