package crackme.vm.core.function

enum class NativeFunctionType(
  val index: Byte,
  val funcName: String
) {
  //functions for test purposes
  TestAddNumbers(255.toByte(), "testAddNumbers"),

  //real functions
  Sizeof(0, "sizeof"),
  Alloc(1, "alloc");

  companion object {
    private val map = mapOf(
      "testAddNumbers" to TestAddNumbers,
      "sizeof" to Sizeof,
      "alloc" to Alloc
    )

    fun fromString(funcName: String): NativeFunctionType? {
      return map[funcName]
    }
  }
}