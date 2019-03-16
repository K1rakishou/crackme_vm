package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.parser.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class IncHandlerTest {

  @Test
  fun testInc() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 15
          inc r0
          inc r0
          inc r0
          inc r0
          inc r0
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(20, vm.registers[0])
  }
}