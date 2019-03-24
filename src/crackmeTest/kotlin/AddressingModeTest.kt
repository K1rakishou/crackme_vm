package sample.helloworld

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMSimulator
import crackme.vm.parser.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class AddressingModeTest {

  @Test
  fun testAddressingModeWithIntVariable() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 0x11223344
          mov r0, ss@[a] as byte
          mov r1, ss@[a + 1] as byte
          mov r2, ss@[a + 2] as byte
          mov r3, ss@[a + 3] as byte

          mov ss@[a + 3] as byte, r0
          mov ss@[a + 2] as byte, r1
          mov ss@[a + 1] as byte, r2
          mov ss@[a] as byte, r3

          mov r0, ss@[a] as dword

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