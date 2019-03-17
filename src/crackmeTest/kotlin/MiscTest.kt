package sample.helloworld

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.parser.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.ParsingException
import platform.windows.TEKPUBKEY
import kotlin.test.Test
import kotlin.test.assertEquals

class MiscTest {

  @Test
  fun testHexNumbers() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 0xDeaDBeeF
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(0xdeadbeef, vm.registers[0])
  }

  @Test
  fun testNegativeHexNumbers() {
    val vmParser = VMParser()

    expectException<ParsingException> {
      vmParser.parse(
        """
          def main()
            mov r0, -0xDeaDBeeF
            ret
          end
        """
      )
    }
  }

}