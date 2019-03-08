package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test

class CallHandlerTest {

  /**
   * Call
   * */

  @Test
  fun test_Call_ConstString() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        use println(String)
        call println("test_Call_ConstString")

        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
  }

  @Test
  fun test_Call_ConstC64() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        use println(String)
        call println(1122334455)

        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
  }

}