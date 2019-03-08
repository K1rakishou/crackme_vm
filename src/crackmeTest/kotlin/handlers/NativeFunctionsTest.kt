package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class NativeFunctionsTest {

  /**
   * Test function
   * */

  @Test
  fun test_Call_test_add_number_func_with_ten_parameters() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        use testAddNumbers(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)

        mov r0, 1
        mov r1, 2
        mov r2, 3
        mov r3, 4
        mov r4, 5

        call testAddNumbers(10, r0, r1, r2, r3, r4, 6, 7, 8, 9, 10)

        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(55, vm.registers[0])
  }

}