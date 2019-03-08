package sample.helloworld

import crackme.vm.core.Utils
import crackme.vm.core.VmMemory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VmMemoryTests {
  private val vmMemory = VmMemory(1024, Random(0))

  @Test
  fun testPutGetInt() {
    vmMemory.putLong(10, 11223344)
    assertEquals(11223344, vmMemory.getLong(10))
  }

  @Test
  fun testPutIntOutOfBounds1() {
    expectException<VmMemory.VmIndexOutOfBoundsException> {
      vmMemory.putLong(-1, 11223344)
    }
  }

  @Test
  fun testPutIntOutOfBounds2() {
    expectException<VmMemory.VmIndexOutOfBoundsException> {
      vmMemory.putLong(66666, 11223344)
    }
  }

  @Test
  fun testGetIntOutOfBounds1() {
    expectException<VmMemory.VmIndexOutOfBoundsException> {
      vmMemory.getLong(-1)
    }
  }

  @Test
  fun testGetIntOutOfBounds2() {
    expectException<VmMemory.VmIndexOutOfBoundsException> {
      vmMemory.getLong(666666)
    }
  }

  @Test
  fun testPutGetLong() {
    vmMemory.putLong(55, 1122334455667788)
    assertEquals(1122334455667788, vmMemory.getLong(55))
  }

  @Test
  fun testAllocateGetString() {
    val str1 = "This is a test string1"
    val str2 = "This is a test string2"

    val address1 = vmMemory.allocString(str1)
    val address2 = vmMemory.allocString(str2)

    val bytes1 = vmMemory.slice(0, 4 + str1.length)
    val bytes2 = vmMemory.slice(4 + str1.length, 4 + str1.length + 4 + str2.length)

    assertEquals("00000016546869732069732061207465737420737472696E6731", Utils.bytesToHex(bytes1))
    assertEquals("00000016546869732069732061207465737420737472696E6732", Utils.bytesToHex(bytes2))

    assertEquals(str1, vmMemory.getString(address1))
    assertEquals(str2, vmMemory.getString(address2))
  }

}
