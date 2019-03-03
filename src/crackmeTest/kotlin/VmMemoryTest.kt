package sample.helloworld

import crackme.vm.core.VmMemory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VmMemoryTests {
  private val vmMemory = VmMemory(1024, Random(0))

  @Test
  fun testPutGetInt() {
    vmMemory.putInt(10, 11223344)
    assertEquals(11223344, vmMemory.getInt(10))
  }

  @Test
  fun testPutIntOutOfBounds1() {
    expectException<VmMemory.VmIndexOutOfBoundsException> {
      vmMemory.putInt(-1, 11223344)
    }
  }

  @Test
  fun testPutIntOutOfBounds2() {
    expectException<VmMemory.VmIndexOutOfBoundsException> {
      vmMemory.putInt(66666, 11223344)
    }
  }

  @Test
  fun testGetIntOutOfBounds1() {
    expectException<VmMemory.VmIndexOutOfBoundsException> {
      vmMemory.getInt(-1)
    }
  }

  @Test
  fun testGetIntOutOfBounds2() {
    expectException<VmMemory.VmIndexOutOfBoundsException> {
      vmMemory.getInt(666666)
    }
  }

  @Test
  fun testPutGetLong() {
    vmMemory.putLong(55, 1122334455667788)
    assertEquals(1122334455667788, vmMemory.getLong(55))
  }

  @Test
  fun testAllocateGetString() {
    val str = "This is a test string"

    val address = vmMemory.allocString(str)
    assertEquals(str, vmMemory.getString(address))
  }
}
