package crackme.vm.instructions

abstract class Instruction {
  fun getInstructionRawSize(): Int = 1 + rawSize()  //1 is instruction type byte

  protected abstract fun rawSize(): Int
}

enum class InstructionType {
  Add,
  Call,
  Cmp,
  Jxx,
  Let,
  Mov,
  Ret
}