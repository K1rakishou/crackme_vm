package crackme.vm.instructions

import crackme.vm.core.AddressingMode
import crackme.vm.operands.Operand

class Pop(
  val addressingMode: AddressingMode,
  override val operand: Operand,
  override val instructionType: InstructionType = InstructionType.Pop
) : GenericOneOperandInstruction, Instruction() {

  //instructionType + addressingMode + size of operand
  override fun rawSize(): Int = 1 + addressingMode.mode + operand.rawSize

  override fun compile(): List<ByteArray> {
    return listOf(
      instructionType.value.toByteArray(),
      operand.compile()
    )
  }

  override fun toString(): String {
    return "pop $operand as ${addressingMode.typeStr}"
  }
}