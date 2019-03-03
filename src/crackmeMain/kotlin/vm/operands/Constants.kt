package crackme.vm.operands

abstract class Constant : Operand

class C32(
  val value: Int
) : Constant() {
  override val name = "C32"

  override fun toString(): String {
    return value.toString()
  }
}

class C64(
  val value: Long
) : Constant() {
  override val name = "C64"

  override fun toString(): String {
    return value.toString()
  }
}

class VmString(
  val memAddress: Int
) : Constant() {
  override val name = "String"

  override fun toString(): String {
    return "string at ($memAddress)"
  }
}