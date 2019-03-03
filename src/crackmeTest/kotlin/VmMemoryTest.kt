package sample.helloworld

import crackme.vm.core.VmMemory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VmMemoryTests {
  private val vmMemory = VmMemory(1024, Random(1234))

  @Test
  fun testPutGetInt() {
    vmMemory.putInt(0, 11223344)
    assertEquals(11223344, vmMemory.getInt(0))
  }

  @Test
  fun testPutGetString() {
    val str = "This is a test string"

    vmMemory.putString(10, str)
    assertEquals(str, vmMemory.getString(10))
  }
}
