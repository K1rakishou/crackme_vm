package crackme.vm.core

enum class VariableType(val str: String) {
  AnyType("Any"),
  IntType("Int"),
  LongType("Long"),
  StringType("String");

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