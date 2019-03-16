package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
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
        def main()
          push 0x11223344
          pop r0
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(0x11223344, vm.registers[0])
  }
}