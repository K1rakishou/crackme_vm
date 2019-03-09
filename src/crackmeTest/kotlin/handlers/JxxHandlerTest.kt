package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class JxxHandlerTest {

  @Test
  fun testSimpleLoop() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r3, 100
        mov r0, 500
@LOOP:
        add r0, 1
        sub r3, 1
        cmp r3, 0
        jne @LOOP
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(600, vm.registers[0])
  }
}