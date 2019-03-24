package sample.helloworld.handlers

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMSimulator
import crackme.vm.core.ParsingException
import crackme.vm.core.ScopeParsingException
import crackme.vm.core.VmExecutionException
import crackme.vm.parser.VMParser
import sample.helloworld.expectException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LetHandlerTest {

  @Test
  fun test_GetVariableAddress() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 1234
          let b: Int, 5678
          let c: Int, 9012
          mov r0, a
          mov r1, b
          mov r2, c
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(0, vm.registers[0])
    assertEquals(4, vm.registers[1])
    assertEquals(8, vm.registers[2])
  }

  @Test
  fun test_GetVariableValue() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 1234
          let b: Int, 5678
          let c: Int, 9012
          mov r0, ss@[a] as dword
          mov r1, ss@[b] as dword
          mov r2, ss@[c] as dword
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(1234, vm.registers[0])
    assertEquals(5678, vm.registers[1])
    assertEquals(9012, vm.registers[2])
  }

  @Test
  fun test_LetIntVariable() {
    //let a: Int, 1234
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 1234
          mov r0, ds@[a] as dword
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(1234, vm.registers[0])
  }

  @Test
  fun test_LetNegativeIntVariable() {
    //let a: Int, -1234
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, -1234
          mov r0, ds@[a] as dword
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(0xFFFFFB2E, vm.registers[0])
  }

  @Test
  fun test_LetIntVariableLongValue() {
    //let a: Int, 7722334455
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Int, 7722334455
          mov r0, ss@[a] as qword
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()

    expectException<VmExecutionException> {
      val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
      vmSimulator.simulate(vm, entryPoint, instructions)
    }
  }

  @Test
  fun test_LetLongVariableIntValue() {
    //let a: Long, 1234
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Long, 1234
          mov r0, ss@[a] as qword
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(1234, vm.registers[0])
  }

  @Test
  fun test_LetLongVariableLongValue() {
    //let a: Long, 77223344556677
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Long, 77223344556677
          mov r0, ss@[a] as qword
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(77223344556677, vm.registers[0])
  }

  @Test
  fun test_LetNegativeLongVariableLongValue() {
    //let a: Long, -77223344556677
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let a: Long, -77223344556677
          mov r0, ss@[a] as qword
          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(-77223344556677, vm.registers[0])
  }


  @Test
  fun test_LetLongVariableVeryLongValue() {
    //let a: Long, 77223344556677889900
    val vmParser = VMParser()

    expectException<ParsingException> {
      vmParser.parse(
        """
          def main()
            let a: Long, 77223344556677889900
            mov r0, ds@[a]
            ret
          end
        """
      )
    }
  }

  @Test
  fun test_MultipleLetInstructions() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def main()
          let d: Int, 1234
          let aavvss: Long, 1122334455667788

          mov r3, d
          mov r3, ss@[r3] as dword

          mov r4, aavvss
          mov r4, ss@[r4] as qword

          ret
        end
      """
    )
    val vmSimulator = VMSimulator()
    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals(1234, vm.registers[3])
    assertEquals(1122334455667788, vm.registers[4])
  }

  @Test
  fun testShouldThrowWhenDetectedMultipleVariablesWithTheSameName() {
    val vmParser = VMParser()

    val exception = expectException<ScopeParsingException> {
      vmParser.parse(
        """
         def main()
           let a: Int, 123
           let a: Int, 123456
           ret
         end
      """
      )
    }

    assertTrue(exception.message!!.contains("Variable (a) is already defined"))
  }

}