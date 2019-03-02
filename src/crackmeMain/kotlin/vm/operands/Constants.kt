package crackme.vm.operands

abstract class Constant : Operand

class C32(
  val value: UInt
) : Constant() {

  override fun toString(): String {
    return value.toString()
  }
}

class C64(
  val value: ULong
) : Constant() {

  override fun toString(): String {
    return value.toString()
  }
}