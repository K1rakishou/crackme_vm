package crackme.vm.operands

abstract class Constant : Operand

class C64(
  val value: Long
) : Constant() {
  override val operandName = "C64"

  override fun toString(): String {
    return value.toString()
  }
}

class VmString(
  val address: Int
) : Constant() {
  override val operandName = "String"

  override fun toString(): String {
    return "string at ($address)"
  }
}