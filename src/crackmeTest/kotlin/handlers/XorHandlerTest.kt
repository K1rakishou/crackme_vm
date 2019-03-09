package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class XorHandlerTest {

  @Test
  fun test_Reg_C32() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 155
        xor r0, 155
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(0, vm.registers[0])
  }

}