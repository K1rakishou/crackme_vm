package crackme.vm.core

enum class NativeFunctionType(val funcName: String) {
  Println("println");

  companion object {
    private val map = mapOf(
      "println" to Println
    )

    fun fromString(funcName: String): NativeFunctionType? {
      return map[funcName]
    }
  }
}