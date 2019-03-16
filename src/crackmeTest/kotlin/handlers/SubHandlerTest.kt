package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.parser.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class SubHandlerTest {

  @Test
  fun test_SubReg_Reg() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 101
          mov r1, 500
          sub r0, r1
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(-399, vm.registers[0])
  }

}