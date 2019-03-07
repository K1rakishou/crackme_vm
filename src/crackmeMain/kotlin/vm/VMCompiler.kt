package crackme.vm

import crackme.vm.core.os.File

class VMCompiler(
  private val vm: VM
) {

  private val REGISTERS_COUNT = vm.registers.size
  private val REGISTERS_OFFSET = 0
  private val SIZE_OF_REGISTER = 8

  fun compile(file: File) {
    
  }

}