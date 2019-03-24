package crackme.vm.operands

interface Operand {
  val operandName: String
  val rawSize: Int
  val operandType: OperandType

  fun compile(): ByteArray
}

enum class OperandType(val value: Byte) {
  Constant_C32(1 shl 0),
  Constant_C64(1 shl 1),
  Register(1 shl 2),
  Variable(1 shl 3),

  //this should be the last bit!!!
  Memory(1 shl 6)
}