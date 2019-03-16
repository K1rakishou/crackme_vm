package sample.helloworld

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.parser.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.VariableType
import kotlin.test.Test
import kotlin.test.assertEquals

class AddressingModeTest {

  @Test
  fun testAddressingModeWithStringVariable() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: String, "GOOD"
          mov r0, ds@[a + 4] as byte
          mov r1, ds@[a + 5] as byte
          mov r2, ds@[a + 6] as byte
          mov r3, ds@[a + 7] as byte

          mov ds@[a + 7] as byte, r0
          mov ds@[a + 6] as byte, r1
          mov ds@[a + 5] as byte, r2
          mov ds@[a + 4] as byte, r3

          ret
        end
      """
    )

    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals("DOOG", vm.vmMemory.getVariableValue("a", VariableType.StringType))
  }

  @Test
  fun testAddressingModeWithIntVariable() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 0x11223344
          mov r0, ds@[a] as byte
          mov r1, ds@[a + 1] as byte
          mov r2, ds@[a + 2] as byte
          mov r3, ds@[a + 3] as byte

          mov ds@[a + 3] as byte, r0
          mov ds@[a + 2] as byte, r1
          mov ds@[a + 1] as byte, r2
          mov ds@[a] as byte, r3

          mov r0, ds@[a] as dword

          ret
        end
      """
    )

    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(0x44332211, vm.registers[0])
  }
}