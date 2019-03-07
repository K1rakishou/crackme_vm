package crackme.vm.core

enum class VariableType(val type: Byte,
                        val str: String) {
  AnyType(0, "Any"),
  LongType(1, "Long"),
  StringType(2, "String");

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