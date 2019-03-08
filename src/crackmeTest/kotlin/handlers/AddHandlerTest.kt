package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class AddHandlerTest {

  /**
   * Add
   * */

  @Test
  fun test_AddReg_Reg() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 101
        mov r1, 500
        add r0, r1
        ret r0
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(601, vm.registers[0])
  }

}