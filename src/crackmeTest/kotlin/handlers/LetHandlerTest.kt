package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class LetHandlerTest {

  @Test
  fun simpleTest() {
    //let a, 0
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Int, 1234
         mov r0, [a]
         ret r0
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(1234, vm.registers[0])

  }

}