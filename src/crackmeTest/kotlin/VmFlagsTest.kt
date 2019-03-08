package sample.helloworld

import crackme.vm.core.VmFlags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VmFlagsTest {

  @Test
  fun testSetFlag() {
    val vmFlags = VmFlags()
    vmFlags.setFlag(VmFlags.Flag.ZF)

    assertEquals(-128, vmFlags.getValue())
  }

  @Test
  fun testResetFlag() {
    val vmFlags = VmFlags(-128)
    vmFlags.resetFlag(VmFlags.Flag.ZF)

    assertEquals(0, vmFlags.getValue())
  }

  @Test
  fun testIsFlagSet() {
    val vmFlags = VmFlags(-128)
    assertTrue(vmFlags.isFlagSet(VmFlags.Flag.ZF))
  }
}