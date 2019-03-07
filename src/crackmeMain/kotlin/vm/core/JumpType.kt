package crackme.vm.core

enum class JumpType(
  val type: Byte,
  val jumpName: String
) {
  Je(0, "je"),
  Jne(1, "jne"),
  Jmp(2, "jmp");

  companion object {
    private val map = mapOf(
      "je" to Je,
      "jne" to Jne,
      "jmp" to Jmp
    )

    fun fromString(str: String): JumpType? {
      return map[str]
    }
  }
}