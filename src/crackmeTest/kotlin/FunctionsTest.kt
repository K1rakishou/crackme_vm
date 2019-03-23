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
        def sum_of_four(a: Long, b: Long, c: Long, d: Long)
          mov r0, ss@[a] as qword
          mov r1, ss@[b] as qword
          mov r2, ss@[c] as qword
          mov r3, ss@[d] as qword

          add r0, r1
          add r0, r2
          add r0, r3

          ret 32
        end

        def main()
          push 10
          push 20
          push 30
          push 100
          call sum_of_four

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

    assertEquals(170, vm.registers[0])
  }
}