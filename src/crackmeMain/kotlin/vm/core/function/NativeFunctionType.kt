package crackme.vm.core.function

enum class NativeFunctionType(
  val index: Byte,
  val funcName: String
) {
  //functions for test purposes
  TestAddNumbers(255.toByte(), "testAddNumbers"),
  Println(254.toByte(), "println"),

  //real functions
  Sizeof(0, "sizeof"),
  Alloc(1, "alloc");

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