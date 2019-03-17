package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.parser.VMParser
import crackme.vm.VMSimulator
import kotlin.test.Test
import kotlin.test.assertEquals

class XorHandlerTest {

  @Test
  fun test_Reg_C32() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 155
          xor r0, 155
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(0, vm.registers[0])
  }

  @Test
  fun test_XorStack_Reg() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          push 0xDEADBEEF112233
          xor ss@[0] as qword, 0xDEADBEEF112233
          pop r1
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(0, vm.registers[1])
  }
}