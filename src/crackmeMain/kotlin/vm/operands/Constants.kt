package crackme.vm.operands

abstract class Constant : Operand

class C64(
  val value: Long
) : Constant() {
  override val operandName = "C64"
  override val rawSize: Int = 8 //value

  override fun toString(): String {
    return value.toString()
  }
}

class VmString(
  val address: Int
) : Constant() {
  override val operandName = "String"
  override val rawSize: Int = 4 //address

  override fun toString(): String {
    return "string at ($address)"
  }
}