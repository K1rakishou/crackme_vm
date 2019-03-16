package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
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
    //FIXME
//    val vmParser = VMParser()
//    val vm = vmParser.parse(
//      """
//        use testAddNumbers(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)
//
//        push 10
//        push 1
//        push 2
//        push 3
//        push 4
//        push 5
//        push 6
//        push 7
//        push 8
//        push 9
//        push 10
//
//        call testAddNumbers
//
//        ret
//      """
//    )
//    val vmSimulator = VMSimulator()
//    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
//    vmSimulator.simulate(vm, entryPoint, instructions)
//
//    assertEquals(55, vm.registers[0])
  }

}