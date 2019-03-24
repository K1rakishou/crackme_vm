package sample.helloworld

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMSimulator
import crackme.vm.parser.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class AddressingModeTest {

  @Test
  fun testAddressingModeWithStringVariable() {
//    val vmParser = VMParser()
//    val vm = vmParser.parse(
//      """
//        def main()
//          let a: String, "GOOD"
//
//          mov r0, ss@[a + 4] as byte
//          mov r0, ds@[r0] as byte
//
//          mov r1, ss@[a + 5] as byte
//          mov r1, ds@[r1] as byte
//
//          mov r2, ss@[a + 6] as byte
//          mov r2, ds@[r2] as byte
//
//          mov r3, ss@[a + 7] as byte
//          mov r3, ds@[r3] as byte
//
//          mov ds@[a + 7] as byte, r0
//          mov ds@[a + 6] as byte, r1
//          mov ds@[a + 5] as byte, r2
//          mov ds@[a + 4] as byte, r3
//
//          mov r0, ds@[a + 4] as dword
//          ret
//        end
//      """
//    )
//
//    val vmSimulator = VMSimulator()
//    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
//    vmSimulator.simulate(vm, entryPoint, instructions)
//
//    println("string = ${vm.vmMemory.getString(0)}")
//    println("hex[0, 64] = ${Utils.bytesToHex(vm.vmMemory.slice(0, 64))}")
//
//    //68 79 79 71 -> DOOG
//    assertEquals(0x68797971 , vm.registers[0])
  }

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