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
        mov r0, [a] as byte
        mov r1, [a + 1] as byte
        mov r2, [a + 2] as byte
        mov r3, [a + 3] as byte

        mov [a + 3] as byte, r0
        mov [a + 2] as byte, r1
        mov [a + 1] as byte, r2
        mov [a] as byte, r3

        ret
      """
    )

    for (instruction in vm.instructions) {
      println(instruction)
    }

    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals("DOOG", vm.vmMemory.getVariableValue("a", VariableType.StringType))
  }
}