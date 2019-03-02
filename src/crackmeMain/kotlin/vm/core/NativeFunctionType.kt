package crackme.vm.core

enum class NativeFunctionType(val funcName: String) {
  VmExit("vm_exit"),
  Println("println");

  companion object {
    private val map = mapOf(
      "vm_exit" to VmExit,
      "println" to Println
    )

    fun fromString(funcName: String): NativeFunctionType? {
      return map[funcName]
    }
  }
}