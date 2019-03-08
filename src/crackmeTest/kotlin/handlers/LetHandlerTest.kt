package sample.helloworld.handlers

import crackme.vm.VMParser
import crackme.vm.VMSimulator
import crackme.vm.core.ParsingException
import crackme.vm.core.VariableType
import crackme.vm.core.VmExecutionException
import sample.helloworld.expectException
import kotlin.test.Test
import kotlin.test.assertEquals

class LetHandlerTest {

  @Test
  fun test_LetIntVariable() {
    //let a: Int, 1234
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Int, 1234
         mov r0, [a]
         ret r0
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
         ret r0
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
         ret r0
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
         ret r0
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
         ret r0
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
         ret r0
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
         ret r0
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
         ret r0
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(string, vm.vmMemory.getVariableValue("a", VariableType.StringType))
  }

}