package crackme.vm.core

enum class AddressingMode(
  val mode: Byte,
  val size: Byte,
  val typeStr: String
) {
  ModeByte(0, 1, "byte"),
  ModeWord(1, 2, "word"),
  ModeDword(2, 4, "dword"),
  ModeQword(3, 8, "qword");

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