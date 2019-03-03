package crackme.vm.core

enum class NativeFunctionType(val funcName: String) {
  TestAddNumbers("testAddNumbers"),

  Println("println"),
  Sizeof("sizeof"),
  Alloc("alloc");

  companion object {
    private val map = mapOf(
      "testAddNumbers" to TestAddNumbers,
      "println" to Println,
      "sizeof" to Sizeof,
      "alloc" to Alloc
    )

    fun fromString(funcName: String): NativeFunctionType? {
      return map[funcName]
    }
  }
}