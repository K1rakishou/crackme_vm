package sample.helloworld

import crackme.misc.extractInstructionsAndGetEntryPoint
import crackme.vm.VMSimulator
import crackme.vm.parser.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalVariablesTest {

  @Test
  fun testGlobalVariablesDefinition() {
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        def_var test1: String = "1234567890"
        def_var test2: String = "awrawtysytuyfi"
        def_var test3: String = "347dtgu67 5usthj zdtjtjriu5ru"

        def main()
          mov r0, test1
          mov r1, test2
          mov r3, test3
          ret
        end
      """
    )

    val vmSimulator = VMSimulator()

    val (instructions, entryPoint) = extractInstructionsAndGetEntryPoint(vm)
    vmSimulator.simulate(vm, entryPoint, instructions)

    assertEquals("1234567890", vm.vmMemory.getString(vm.vmMemory.getVariable("test1")!!.address))
    assertEquals("awrawtysytuyfi", vm.vmMemory.getString(vm.vmMemory.getVariable("test2")!!.address))
    assertEquals("347dtgu67 5usthj zdtjtjriu5ru", vm.vmMemory.getString(vm.vmMemory.getVariable("test3")!!.address))
  }
}