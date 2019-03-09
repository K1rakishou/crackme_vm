package crackme.vm.operands

import crackme.vm.core.AddressingMode
import crackme.vm.core.Utils
import kotlin.experimental.or

class Memory<T : Operand>(
  val operand: T,
  val offsetOperand: T? = null,
  val addressingMode: AddressingMode = AddressingMode.ModeQword
) : Operand {
  override val operandName = "Memory"
  override val rawSize: Int = 1 + operand.rawSize + (offsetOperand?.rawSize ?: 0) + 1 //operandType + operandSize (or 0) + offsetOperandSize + addressingMode
  override val operandType: OperandType = OperandType.Memory

  override fun compile(): ByteArray {
    val operandBytes = operand.compile()
    val offsetOperandBytes = offsetOperand?.compile()

    val completeBytes = ByteArray(rawSize)
    var offset = 0

    completeBytes[offset] = operandType.value or operand.operandType.value
    ++offset

    Utils.copyBytes(operandBytes, 0, completeBytes, offset, operandBytes.size)
    offset += operandBytes.size

    if (offsetOperandBytes != null) {
      Utils.copyBytes(offsetOperandBytes, 0, offsetOperandBytes, offset, offsetOperandBytes.size)
      offset += offsetOperandBytes.size
    }

    completeBytes[offset] = addressingMode.mode

    return completeBytes
  }

  override fun toString(): String {
    val offset = if (offsetOperand != null) {
      " + $offsetOperand"
    } else {
      ""
    }

    return "[$operand$offset] as ${addressingMode.typeStr}"
  }
}