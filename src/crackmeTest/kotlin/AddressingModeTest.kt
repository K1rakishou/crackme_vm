package sample.helloworld

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.VariableType
import kotlin.test.Test
import kotlin.test.assertEquals

class AddressingModeTest {

  @Test
  fun testAddressingMode() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        let a: String, "GOOD"
        mov r0, [a + 4] as byte
        mov r1, [a + 5] as byte
        mov r2, [a + 6] as byte
        mov r3, [a + 7] as byte

        mov [a + 7] as byte, r0
        mov [a + 6] as byte, r1
        mov [a + 5] as byte, r2
        mov [a + 4] as byte, r3

        ret
      """
    )

    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals("DOOG", vm.vmMemory.getVariableValue("a", VariableType.StringType))
  }
}