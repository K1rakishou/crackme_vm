package handlers

import crackme.vm.VMSimulator
import crackme.vm.VMParser
import kotlin.test.Test
import kotlin.test.assertEquals

class MovHandlerTest {

  //TODO: implement GenericTwoOperandsInstructionHandlerTester?

  /**
   * Mov
   * */

  //Reg_C64
  @Test
  fun test_MovReg_Const64() {
    //mov r0, 123L

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 1122334455667788
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(1122334455667788L, vm.registers[0])
  }

  //Reg_C32
  @Test
  fun test_MovReg_Const32() {
    //mov r0, 123

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 11223344
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(11223344L, vm.registers[0])
  }

  //TODO: is this even possible? Maybe this should be removed completely?
  //Reg_MemC64

  //Reg_MemC32
  @Test
  fun test_MovReg_MemConst() {
    //mov r0, [11223344]
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, [0]
        ret
      """
    )
    vm.vmMemory.putInt(0, 112233)
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(112233, vm.registers[0])
  }

  //Reg_MemReg
  @Test
  fun test_MovReg_MemReg() {
    //mov r0, [r0]

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
          mov r1, 0
          mov r0, [r1]
          ret
        """
    )
    vm.vmMemory.putLong(0, 112233)
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(112233, vm.registers[0])
  }

  //Reg_MemVar
  @Test
  fun test_MovReg_MemVar() {
    //mov r0, [abc]
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
          let a: Int, 112233
          mov r0, [a]
          ret
        """
    )

    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(112233, vm.registers[0])
  }

  //Reg_Reg
  @Test
  fun test_MovReg_Reg() {
    //mov r0, r1

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r1, 112233
        mov r0, r1
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(112233, vm.registers[0])
  }

  //Reg_Var
  @Test
  fun test_MovReg_Var() {
    //instr r0, abc

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Int, 0
         mov r0, a
         ret
      """
    )

    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    //first allocation should be at 0 address
    assertEquals(0, vm.registers[0])
  }

  //  MemReg_Reg
  @Test
  fun test_MovMemReg_Reg() {
    //mov [r0], r0

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 112233
        mov r1, 0
        mov [r1], r0
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(112233, vm.vmMemory.getLong(0))
  }


  //MemVar_Reg
  @Test
  fun test_MovMemVar_Reg() {
    //mov [abc], r0
    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
         let a: Int, 0
         mov r1, 334455
         mov [a], r1
         mov r0, [a]
         ret
      """
    )

    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(334455, vm.registers[0])
  }

  //TODO: is this even possible? Maybe this should be removed completely?
  //MemC64_Reg

  //  MemC32_Reg
  @Test
  fun test_MovMemConst_Reg() {
    //mov [123], r0

    val vmParser = VMParser()
    val vm = vmParser.parse(
      """
        mov r0, 112233
        mov [0], r0
        ret
      """
    )
    val vmSimulator = VMSimulator()
    vmSimulator.simulate(vm)

    assertEquals(112233, vm.vmMemory.getInt(0))
  }


}