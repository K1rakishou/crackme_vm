package crackme.vm.operands

import crackme.vm.core.Utils
import kotlin.experimental.or

class Memory(
  val operand: Operand
) : Operand {
  override val operandName = "Memory"
  override val rawSize: Int = 1 + operand.rawSize //operandType + size of the operand
  override val operandType: OperandType = OperandType.Memory

  override fun compile(): ByteArray {
    val operandBytes = operand.compile()
    val completeBytes = ByteArray(operandBytes.size + 1)

    completeBytes[0] = operandType.value or operand.operandType.value
    Utils.copyBytes(operandBytes, 0, completeBytes, 1, operandBytes.size)

    return completeBytes
  }

  override fun toString(): String {
    return operand.toString()
  }
}