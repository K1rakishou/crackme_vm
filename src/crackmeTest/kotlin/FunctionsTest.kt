package sample.helloworld

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMSimulator
import crackme.vm.parser.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionsTest {

  @Test
  fun testSimpleFunction() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def sum_of_three(a: Long, b: Long, c: Long)
          mov r0, ss@[a] as qword
          mov r1, ss@[b] as qword
          mov r2, ss@[c] as qword

          add r0, r1
          add r0, r2

          ret 0x18
        end

        def main()
          push 10
          push 20
          push 30
          call sum_of_three

          mov r1, 10
@LOOP:
          inc r0
          dec r1
          jne @LOOP

          ret
        end
      """
    )

    val vmSimulator = VMSimulator()

    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(70, vm.registers[0])
  }
}