package crackme.vm.core

enum class AddressingMode(
  val mode: Byte,
  val typeStr: String
) {
  ModeByte(0, "byte"),
  ModeWord(1, "word"),
  ModeDword(2, "dword"),
  ModeQword(3, "qword");

  companion object {
    private val map = mapOf(
      "byte" to ModeByte,
      "word" to ModeWord,
      "dword" to ModeDword,
      "qword" to ModeQword
    )

    fun fromString(typeStr: String): AddressingMode? {
      return map[typeStr]
    }
  }
}