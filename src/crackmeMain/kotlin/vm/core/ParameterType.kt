package crackme.vm.core

enum class ParameterType(val str: String) {
  IntType("Int"),
  LongType("Long"),
  FloatType("Float"),
  DoubleType("Double"),
  StringType("String");

  companion object {
    private val map = mapOf(
      "Int" to IntType,
      "Long" to LongType,
      "Float" to FloatType,
      "Double" to DoubleType,
      "String" to StringType
    )

    fun fromString(str: String): ParameterType? {
      return map[str]
    }
  }
}