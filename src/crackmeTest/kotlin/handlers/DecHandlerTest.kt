package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class DecHandlerTest {

  @Test
  fun testDec() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 15
        dec r0
        dec r0
        dec r0
        dec r0
        dec r0
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(10, vm.registers[0])
  }

}