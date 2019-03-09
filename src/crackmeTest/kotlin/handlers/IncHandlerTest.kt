package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class IncHandlerTest {

  @Test
  fun testInc() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 15
        inc r0
        inc r0
        inc r0
        inc r0
        inc r0
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(20, vm.registers[0])
  }
}