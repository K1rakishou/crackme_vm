package crackme.vm.core

enum class VariableType(val type: Byte,
                        val str: String) {
  //TODO add Numeric (or AnyNumber) type to auto convert between C32 and C64 when we don't care about their type
  IntType(0, "Int"),
  LongType(1, "Long"),
  StringType(2, "String");

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