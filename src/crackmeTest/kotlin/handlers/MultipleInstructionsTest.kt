package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class MultipleInstructionsTest {

  /**
   * Multiple instructions
   * */

  @Test
  fun test_MultipleInstructions() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 0
        mov r1, 11
        mov r2, 22
        mov r3, 33
        mov r4, 44
        mov r5, 55

        add r0, r1
        add r0, r2
        add r0, r3
        add r0, r4
        add r0, r5

        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(165, vm.registers[0])
  }

}