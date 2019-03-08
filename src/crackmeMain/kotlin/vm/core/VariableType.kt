package crackme.vm.core

enum class VariableType(val type: Byte,
                        val str: String) {
  //TODO add Numeric (or AnyNumber) type to auto convert between C32 and C64 when we don't care about their type
  AnyType(0, "Any"),
  IntType(1, "Int"),
  LongType(2, "Long"),
  StringType(3, "String");

  companion object {
    private val map = mapOf(
      "Any" to AnyType,
      "Int" to IntType,
      "Long" to LongType,
      "String" to StringType
    )

    fun fromString(str: String): VariableType? {
      return map[str]
    }
  }
}