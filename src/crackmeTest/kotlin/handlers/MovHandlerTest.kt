package handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMSimulator
import crackme.vm.parser.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class MovHandlerTest {

  //TODO: implement GenericTwoOperandsInstructionHandlerTester?

  /**
   * Mov
   * */

  @Test
  fun test_MovReg_Const64() {
    //mov r0, 123L

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 1122334455667788
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(1122334455667788L, vm.registers[0])
  }

  @Test
  fun test_MovReg_Const32() {
    //mov r0, 123

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 11223344
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(11223344L, vm.registers[0])
  }

  @Test
  fun test_MovReg_MemConst() {
    //mov r0, ds@[11223344]
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, ds@[0] as dword
          ret
        end
      """
    )
    vm.vmMemory.putInt(0, 112233)
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(112233, vm.registers[0])
  }

  @Test
  fun test_MovReg_MemReg() {
    //mov r0, ds@[r0]

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r1, 0
          mov r0, ds@[r1] as dword
          ret
        end
      """
    )
    vm.vmMemory.putInt(0, 112233)
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(112233, vm.registers[0])
  }

  @Test
  fun test_MovReg_MemVar() {
    //mov r0, ds@[abc]
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 112233
          mov r0, ds@[a] as dword
          ret
        end
      """
    )

    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(112233, vm.registers[0])
  }

  @Test
  fun test_MovReg_Reg() {
    //mov r0, r1

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r1, 112233
          mov r0, r1
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(112233, vm.registers[0])
  }

  @Test
  fun test_MovReg_Var() {
    //instr r0, abc

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 0
          mov r0, a
          ret
        end
      """
    )

    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    //first allocation should be at 0 address
    assertEquals(0, vm.registers[0])
  }

  @Test
  fun test_MovMemReg_Reg() {
    //mov ds@[r0], r0

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 112233
          mov r1, 0
          mov ds@[r1] as dword, r0
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(112233, vm.vmMemory.getInt(0))
  }


  @Test
  fun test_MovMemVar_Reg() {
    //mov ds@[abc], r0
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 0
          mov r1, 334455
          mov ds@[a] as dword, r1
          mov r0, ds@[a] as dword
          ret
        end
      """
    )

    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(334455, vm.registers[0])
  }

  @Test
  fun test_MovMemConst_Reg() {
    //mov ds@[123], r0

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r0, 112233
          mov ds@[0] as dword, r0
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(112233, vm.vmMemory.getInt(0))
  }

  @Test
  fun test_MovStackConst_Reg() {
    //mov ss@[123], r0

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          mov r1, 112233
          push 0x11223344AABBCCDD
          mov ss@[0] as qword, r1
          mov r0, ss@[0] as qword
          pop r1
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(112233, vm.registers[0])
    assertEquals(112233, vm.registers[1])
  }

}