package crackme.vm.operands

interface Operand {
  val operandName: String
  val rawSize: Int
  val operandType: OperandType

  fun compile(): ByteArray
}

enum class OperandType(val value: Byte) {
  Memory(0 shl 7),
  Constant_C64(0 shl 1),
  Constant_VmString(0 shl 2),
  Register(0 shl 3),
  Variable(0 shl 4)
}