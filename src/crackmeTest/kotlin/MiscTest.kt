package sample.helloworld

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.ParsingException
import kotlin.test.Test
import kotlin.test.assertEquals

class MiscTest {

  @Test
  fun testHexNumbers() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 0xDeaDBeeF
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(0xdeadbeef, vm.registers[0])
  }

  @Test
  fun testNegativeHexNumbers() {
    val vmParser = VMParser()

    expectException<ParsingException> {
      vmParser.parse(
        """
          mov r0, -0xDeaDBeeF
          ret
        """
      )
    }
  }

}