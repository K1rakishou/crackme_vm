package crackme.vm.operands

class Register(
  val index: Int
) : Operand {
  override val operandName = "Register"
  override val rawSize = 1 + 1 //operandType + index
  override val operandType: OperandType = OperandType.Register

  override fun compile(): ByteArray {
    val array = ByteArray(rawSize)

    array[0] = operandType.value
    array[1] = (index and 0xFF).toByte()

    return array
  }

  override fun toString(): String {
    return "r$index"
  }
}