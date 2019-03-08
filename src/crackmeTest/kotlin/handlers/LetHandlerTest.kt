package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.ParsingException
import crackme.vm.core.VariableType
import crackme.vm.core.VmExecutionException
import sample.helloworld.expectException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LetHandlerTest {

  @Test
  fun test_LetIntVariable() {
    //let a: Int, 1234
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Int, 1234
         mov r0, [a]
         ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(1234, vm.registers[0])
  }

  @Test
  fun test_LetNegativeIntVariable() {
    //let a: Int, -1234
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Int, -1234
         mov r0, [a]
         ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(-1234, vm.registers[0])
  }

  @Test
  fun test_LetIntVariableLongValue() {
    //let a: Int, 7722334455
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Int, 7722334455
         mov r0, [a]
         ret
      """
    )
    val vmSimulator = VMSimulator()

    expectException<VmExecutionException> {
      vmSimulator.simulate(vm)
    }
  }

  @Test
  fun test_LetLongVariableIntValue() {
    //let a: Long, 1234
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Long, 1234
         mov r0, [a]
         ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(1234, vm.registers[0])
  }

  @Test
  fun test_LetLongVariableLongValue() {
    //let a: Long, 77223344556677
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Long, 77223344556677
         mov r0, [a]
         ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(77223344556677, vm.registers[0])
  }

  @Test
  fun test_LetNegativeLongVariableLongValue() {
    //let a: Long, -77223344556677
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Long, -77223344556677
         mov r0, [a]
         ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)
    assertEquals(-77223344556677, vm.registers[0])
  }


  @Test
  fun test_LetLongVariableVeryLongValue() {
    //let a: Long, 77223344556677889900
    val vmParser = VMParser()

    expectException<ParsingException> {
      vmParser.parse(
        """
         let a: Long, 77223344556677889900
         mov r0, [a]
         ret
      """
      )
    }
  }

  @Test
  fun test_LetString() {
    //let a: String, "Hello from VM!"
    val string = "Hello from VM!"

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: String, "${string}"
         ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(string, vm.vmMemory.getVariableValue("a", VariableType.StringType))
  }

  @Test
  fun test_MultipleLetInstructions() {
    val string1 = "Hello from VM!"
    val string2 = "This is a test string #2"
    val string3 = "And this is another test string 45346346347347"

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: String, "${string1}"
         let b: String, "${string2}"
         let c: String, "${string3}"
         let d: Int, 1234
         let aavvss: Long, 1122334455667788
         ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(string1, vm.vmMemory.getVariableValue("a", VariableType.StringType))
    assertEquals(string2, vm.vmMemory.getVariableValue("b", VariableType.StringType))
    assertEquals(string3, vm.vmMemory.getVariableValue("c", VariableType.StringType))
    assertEquals(1234, vm.vmMemory.getVariableValue("d", VariableType.IntType))
    assertEquals(1122334455667788, vm.vmMemory.getVariableValue("aavvss", VariableType.LongType))
  }

  @Test
  fun testShouldThrowWhenDetectedMultipleVariablesWithTheSameName() {
    val vmParser = VMParser()

    val exception = expectException<ParsingException> {
      vmParser.parse(
        """
         let a: Int, 123
         let a: Int, 123456
         ret
      """
      )
    }

    assertTrue(exception.message!!.contains("Variable (a) is already defined"))
  }

}