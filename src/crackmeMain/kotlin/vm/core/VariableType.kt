package crackme.vm.core

enum class VariableType(val str: String) {
  AnyType("Any"),
  LongType("Long"),
  StringType("String");

  companion object {
    private val map = mapOf(
      "Any" to AnyType,
      "Long" to LongType,
      "String" to StringType
    )

    fun fromString(str: String): VariableType? {
      return map[str]
    }
  }
}