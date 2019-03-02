package crackme.vm.core

enum class JumpType(val type: String) {
  Je("je"),
  Jne("jne"),
  Jmp("jmp");

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