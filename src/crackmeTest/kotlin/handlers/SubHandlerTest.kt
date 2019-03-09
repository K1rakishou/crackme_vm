package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class SubHandlerTest {

  @Test
  fun test_SubReg_Reg() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 101
        mov r1, 500
        sub r0, r1
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(-399, vm.registers[0])
  }

}