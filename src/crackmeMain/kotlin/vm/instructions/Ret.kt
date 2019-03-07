package crackme.vm.instructions

import crackme.vm.operands.Register

class Ret(
  val result: Register
) : Instruction() {
  override fun rawSize(): Int = result.rawSize

  override fun toString(): String {
    return "ret $result"
  }
}