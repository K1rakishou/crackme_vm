package crackme.vm.operands

abstract class Constant : Operand

class C32(
  val value: Int
) : Constant() {

  override fun toString(): String {
    return value.toString()
  }
}

class C64(
  val value: Long
) : Constant() {

  override fun toString(): String {
    return value.toString()
  }
}

class VmString(
  val memAddress: Int,
  val len: Int
) : Constant() {

  override fun toString(): String {
    return "string at ($memAddress) with len ($len)"
  }
}