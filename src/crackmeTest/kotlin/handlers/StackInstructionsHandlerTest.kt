package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class StackInstructionsHandlerTest {

  @Test
  fun testSimplePushPop() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        push 0x11223344
        pop r0
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(0x11223344, vm.registers[0])
  }
}