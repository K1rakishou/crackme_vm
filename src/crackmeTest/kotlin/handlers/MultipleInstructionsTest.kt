package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.VariableType
import kotlin.test.Test
import kotlin.test.assertEquals

class MultipleInstructionsTest {

  /**
   * Multiple instructions
   * */

  @Test
  fun test_MultipleInstructions() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 0
          mov r1, 11
          mov r2, 22
          mov r3, 33
          mov r4, 44
          mov r5, 55

          add r0, r1
          add r0, r2
          add r0, r3
          add r0, r4
          add r0, r5

          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(165, vm.registers[0])
  }

  @Test
  fun testEncryptDecryptString() {
    val text = "Cipher text. awr awr aw0ith a98tyha973hytou3htouis3htous3htouh"

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let originalString: String, "$text"
          mov r0, originalString
          add r0, 4
          mov r1, [originalString] as dword

  @ENCRYPTION_LOOP:
          xor [r0 + r1] as byte, 0x55
          dec r1
          jne @ENCRYPTION_LOOP

          mov r1, [originalString] as dword

  @DECRYPTION_LOOP:
          xor [r0 + r1] as byte, 0x55
          dec r1
          jne @DECRYPTION_LOOP

          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    val resultString = vm.vmMemory.getVariableValue<String>("originalString", VariableType.StringType)
    assertEquals(text, resultString)
  }
}