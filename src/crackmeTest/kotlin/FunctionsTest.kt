package sample.helloworld

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionsTest {

  @Test
  fun testSimpleFunction() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def sum_of_three(a: Int, b: Int, c: Int)
          pop r7
          pop r0
          pop r1
          pop r2
          push r7

          add r0, r1
          add r0, r2

          ret
        end

        def main()
          push 10
          push 20
          push 30
          call sum_of_three

          mov r1, 10
@LOOP:
          dec r1
          jne @LOOP

          ret
        end
      """
    )

    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(60, vm.registers[0])
  }
}