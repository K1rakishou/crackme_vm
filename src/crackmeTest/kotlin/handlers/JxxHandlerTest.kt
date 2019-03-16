package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
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
        def main()
          mov r3, 100
          mov r0, 500
  @LOOP:
          add r0, 1
          sub r3, 1
          cmp r3, 0
          jne @LOOP
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(600, vm.registers[0])
  }

  @Test
  fun testSimpleLoopWithDec() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r3, 100
          mov r0, 200
  @LOOP:
          add r0, 1
          dec r3
          dec r3
          cmp r3, 0
          jne @LOOP
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(250, vm.registers[0])
  }
}