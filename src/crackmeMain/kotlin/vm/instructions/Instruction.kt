package crackme.vm.instructions

interface Instruction

enum class InstructionType {
  Add,
  Call,
  Cmp,
  Jxx,
  Let,
  Mov,
  Ret
}