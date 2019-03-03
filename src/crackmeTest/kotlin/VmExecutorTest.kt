package sample.helloworld

import crackme.vm.VMExecutor
import crackme.vm.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class VmExecutorTest {

  @Test
  fun test_MovReg_Const32() {
    //mov r0, 123

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 11223344
        ret r0
      """
    )
    val vmExecutor = VMExecutor(vm)

    assertEquals(11223344L, vmExecutor.run())
  }

  @Test
  fun test_MovReg_Const64() {
    //mov r0, 123L

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 1122334455667788
        ret r0
      """
    )
    val vmExecutor = VMExecutor(vm)

    assertEquals(1122334455667788L, vmExecutor.run())
  }

  @Test
  fun test_MovReg_Reg() {
    //mov r0, r1

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r1, 112233
        mov r0, r1
        ret r0
      """
    )
    val vmExecutor = VMExecutor(vm)

    assertEquals(112233, vmExecutor.run())
  }

  @Test
  fun test_MovReg_MemConst() {
    //mov r0, [123]

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, [0]
        ret r0
      """
    )
    vm.vmMemory.putLong(0, 112233)
    val vmExecutor = VMExecutor(vm)

    assertEquals(112233, vmExecutor.run())
  }

  @Test
  fun test_MovReg_MemReg() {
    //mov r0, [r0]

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r1, 0
        mov r0, [r1]
        ret r0
      """
    )
    vm.vmMemory.putLong(0, 112233)
    val vmExecutor = VMExecutor(vm)

    assertEquals(112233, vmExecutor.run())
  }

  @Test
  fun test_MovMemConst_Reg() {
    //mov [123], r0

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 112233
        mov [0], r0
        ret r0
      """
    )
    val vmExecutor = VMExecutor(vm)
    vmExecutor.run()

    assertEquals(112233, vm.vmMemory.getLong(0))
  }

  @Test
  fun test_MovMemReg_Reg() {
    //mov [r0], r0

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 112233
        mov r1, 0
        mov [r1], r0
        ret r0
      """
    )
    val vmExecutor = VMExecutor(vm)
    vmExecutor.run()

    assertEquals(112233, vm.vmMemory.getLong(0))
  }
}