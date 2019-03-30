package sample.helloworld

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMSimulator
import crackme.vm.parser.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class FunctionsTest {

  @Test
  fun testSimpleFunction() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def sum_of_four(a: Int, b: Int, c: Int, d: Int)
          mov r0, ss@[a] as dword
          mov r1, ss@[b] as dword
          mov r2, ss@[c] as dword
          mov r3, ss@[d] as dword

          add r0, r1
          add r0, r2
          add r0, r3

          ret 16
        end

        def main()
          pushd 10
          pushd 20
          pushd 30
          pushd 100
          call sum_of_four

          mov r1, 10
@LOOP:
          inc r0
          dec r1
          jne @LOOP

          ret
        end
      """
    )

    val vmSimulator = VMSimulator()

    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(170, vm.registers[0])
  }

  @Test
  fun testSimpleFunction2() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def sum_of_four(a: Long, b: Long, c: Long, d: Long)
          mov r0, ss@[a] as qword

          add r0, ss@[b] as qword
          add r0, ss@[c] as qword
          add r0, ss@[d] as qword

          ret 32
        end

        def main()
          pushq 10
          pushq 20
          pushq 30
          pushq 100
          call sum_of_four

          mov r1, 10
@LOOP:
          inc r0
          dec r1
          jne @LOOP

          ret
        end
      """
    )

    val vmSimulator = VMSimulator()

    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(170, vm.registers[0])
  }
}