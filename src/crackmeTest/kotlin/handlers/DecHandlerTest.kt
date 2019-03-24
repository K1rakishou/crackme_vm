package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMSimulator
import crackme.vm.parser.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class DecHandlerTest {

  @Test
  fun testDec() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 15
          dec r0
          dec r0
          dec r0
          dec r0
          dec r0
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(10, vm.registers[0])
  }

  @Test
  fun testDecStackReg() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 10
          mov r2, a
          dec ss@[r2] as dword
          dec ss@[r2] as dword
          dec ss@[r2] as dword
          dec ss@[r2] as dword
          dec ss@[r2] as dword
          mov r4, ss@[r2] as dword

          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(5, vm.registers[4])
  }

  @Test
  fun testDecStackConstant() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 0x11223344
          dec ss@[0] as dword
          dec ss@[0] as dword
          dec ss@[0] as dword
          dec ss@[0] as dword
          dec ss@[0] as dword
          mov r2, ss@[0] as dword

          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(0x1122333F, vm.registers[2])
  }

}